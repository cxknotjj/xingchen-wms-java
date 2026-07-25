package org.jeecg.modules.wms.inorder.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrderItemsMapper;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrdersMapper;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.inorder.vo.WmsStockInOrdersPage;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Description: 入库单主表
 * @Author: jeecg-boot
 * @Date:   2025-09-03
 * @Version: V1.0
 */
@Service
public class WmsStockInOrdersServiceImpl extends ServiceImpl<WmsStockInOrdersMapper, WmsStockInOrders> implements IWmsStockInOrdersService {

	@Autowired
	private WmsStockInOrdersMapper wmsStockInOrdersMapper;
	@Autowired
	private WmsStockInOrderItemsMapper wmsStockInOrderItemsMapper;

	@Autowired
	protected IWmsStockInOrderItemsService stockInOrderItemsService;

	@Autowired
	private RedisUtil redisUtil;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(WmsStockInOrders wmsStockInOrders, List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
		wmsStockInOrdersMapper.insert(wmsStockInOrders);
		if(wmsStockInOrderItemsList!=null && wmsStockInOrderItemsList.size()>0) {
			for(WmsStockInOrderItems entity:wmsStockInOrderItemsList) {
				//外键设置
				entity.setOrderId(wmsStockInOrders.getId());
				wmsStockInOrderItemsMapper.insert(entity);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(WmsStockInOrders wmsStockInOrders,List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
		String status = wmsStockInOrders.getStatus();
		//入库单初始状态、审核失败状态可以修改
		if(!status.equals(WarehouseDictEnum.INBOUND_INITIAL.getCode())
				&& !status.equals(WarehouseDictEnum.INBOUND_REJECTED.getCode())){
			throw new JeecgBootException("入库单状态为“创建”或审核失败状态方可修改");
		}
		//入库单明细至少添加一条
		if(wmsStockInOrderItemsList==null || wmsStockInOrderItemsList.size()<1){
			throw new JeecgBootException("入库单明细至少添加一条");
		}

		//1.先删除子表数据
		wmsStockInOrderItemsMapper.deleteByMainId(wmsStockInOrders.getId());

		//定义一个汇总后的商品明细list
		List<WmsStockInOrderItems> mergeList = new ArrayList<>();

		//对子表数据进行汇总
		//根据商品id汇总 <key 商品id,value 商品列表>
		Map<String, List<WmsStockInOrderItems>> collect = wmsStockInOrderItemsList.stream().collect(Collectors.groupingBy(WmsStockInOrderItems::getProductId));
		collect.entrySet().stream().forEach(entry -> {
			//商品id
			String key = entry.getKey();
			List<WmsStockInOrderItems> value = entry.getValue();
			if(value.size()>1){
				//汇总
				int expectedQuantitySum = value.stream().mapToInt(WmsStockInOrderItems::getExpectedQuantity).sum();
				WmsStockInOrderItems wmsStockInOrderItems = new WmsStockInOrderItems();
				BeanUtils.copyProperties(value.get(0),wmsStockInOrderItems);
				wmsStockInOrderItems.setProductId(key);
				wmsStockInOrderItems.setExpectedQuantity(expectedQuantitySum);
				wmsStockInOrderItems.setOrderId(wmsStockInOrders.getId());
				mergeList.add(wmsStockInOrderItems);
			}else{
				WmsStockInOrderItems wmsStockInOrderItems = value.get(0);
				wmsStockInOrderItems.setOrderId(wmsStockInOrders.getId());
				mergeList.add(wmsStockInOrderItems);
			}
		});


		//2.子表数据重新插入
		for(WmsStockInOrderItems entity:mergeList) {
			//外键设置
			entity.setOrderId(wmsStockInOrders.getId());
			wmsStockInOrderItemsMapper.insert(entity);
		}
		//将mergeList里边的采购数量进行汇总
		int totalExpectedQuantity = mergeList.stream().mapToInt(WmsStockInOrderItems::getExpectedQuantity).sum();
		wmsStockInOrders.setTotalExpectedQuantity(totalExpectedQuantity);
		//修改入库单表的数据
		wmsStockInOrdersMapper.updateById(wmsStockInOrders);
	}
//	@Override
//	@Transactional(rollbackFor = Exception.class)
//	public void updateMain(WmsStockInOrders wmsStockInOrders,List<WmsStockInOrderItems> wmsStockInOrderItemsList) {
//		//修改入库单表的数据
//		wmsStockInOrdersMapper.updateById(wmsStockInOrders);
//
//		//1.先删除子表数据
//		wmsStockInOrderItemsMapper.deleteByMainId(wmsStockInOrders.getId());
//
//		//2.子表数据重新插入
//		if(wmsStockInOrderItemsList!=null && wmsStockInOrderItemsList.size()>0) {
//			for(WmsStockInOrderItems entity:wmsStockInOrderItemsList) {
//				//外键设置
//				entity.setOrderId(wmsStockInOrders.getId());
//				wmsStockInOrderItemsMapper.insert(entity);
//			}
//		}
//	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		wmsStockInOrderItemsMapper.deleteByMainId(id);
		wmsStockInOrdersMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			wmsStockInOrderItemsMapper.deleteByMainId(id.toString());
			wmsStockInOrdersMapper.deleteById(id);
		}
	}

	@Override
	public void add(WmsStockInOrders wmsStockInOrders) {

		//生成入库单号 ASN+8位年月日+4位序号
		String orderNumber = generateOrderNumber();
		wmsStockInOrders.setOrderNumber(orderNumber);
		baseMapper.insert(wmsStockInOrders);

	}

	/**
	 * 审核入库单
	 * @param wmsStockInOrdersPage
	 */
	public void audit(WmsStockInOrdersPage wmsStockInOrdersPage){
		//如果没有选择入库单则返回错误
		if(oConvertUtils.isEmpty(wmsStockInOrdersPage.getId())){
			//抛出异常
			throw new JeecgBootException("请选择要审核的入库单");
		}
		WmsStockInOrders wmsStockInOrdersEntity = getById(wmsStockInOrdersPage.getId());
		if(wmsStockInOrdersEntity==null) {
			//异常
			throw new JeecgBootException("入库单不存在");
		}
		//入库单明细
		List<WmsStockInOrderItems> wmsStockInOrderItemsList = stockInOrderItemsService.selectByMainId(wmsStockInOrdersPage.getId());
		if(wmsStockInOrderItemsList==null || wmsStockInOrderItemsList.size()<=0){
			//异常
			throw new JeecgBootException("请添加入库明细");
		}
		//当前状态只能是提交审核状态
		if(!WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode().equals(wmsStockInOrdersEntity.getStatus())){
			//异常
			throw new JeecgBootException("当前状态是提交审核状态方可进行审核");
		}
		//更新状态为审核通过或审核不通过
		wmsStockInOrdersEntity.setStatus(wmsStockInOrdersPage.getStatus());
		updateById(wmsStockInOrdersEntity);
	}
	/**
	 * 提交审核
	 * @param wmsStockInOrdersPage
	 */
	public void submitAudit(WmsStockInOrdersPage wmsStockInOrdersPage){
		//如果没有选择入库单则返回错误
		if(wmsStockInOrdersPage==null || oConvertUtils.isEmpty(wmsStockInOrdersPage.getId())){
			//抛出异常
			throw new JeecgBootException("请选择要审核的入库单");
		}
		WmsStockInOrders wmsStockInOrdersEntity = getById(wmsStockInOrdersPage.getId());
		if(wmsStockInOrdersEntity==null) {
			//异常
			throw new JeecgBootException("入库单不存在");
		}
		//当前状态只能是初始状态、审核失败状态
		if(!(WarehouseDictEnum.INBOUND_INITIAL.getCode().equals(wmsStockInOrdersEntity.getStatus())
				|| WarehouseDictEnum.INBOUND_REJECTED.getCode().equals(wmsStockInOrdersEntity.getStatus()))){
			throw new JeecgBootException("非初始状态、审核失败状态入库单不允许审核");
		}
		//更新状态为提交审核
		wmsStockInOrdersEntity.setStatus(WarehouseDictEnum.INBOUND_SUBMIT_AUDIT.getCode());
		updateById(wmsStockInOrdersEntity);
	}

    @Override
    public String updateReceivedStatus(String stockInOrderId) {

		//查询入库单
		WmsStockInOrders wmsStockInOrders = getById(stockInOrderId);

		//查询入库单明细
		List<WmsStockInOrderItems> wmsStockInOrderItems = stockInOrderItemsService.selectByMainId(stockInOrderId);

		//只要存在一个未收货完成的记录，状态就是未收货完成，否则 就是收货完成
		boolean b = wmsStockInOrderItems.stream().anyMatch(item -> !item.getStatus().equals(WarehouseDictEnum.INBOUND_DETAIL_RECEIVED.getCode()));
		String status = null;

		if(b){
			status = WarehouseDictEnum.INBOUND_RECEIVING.getCode();
		}else{
			status = WarehouseDictEnum.INBOUND_RECEIVED.getCode();
		}
		wmsStockInOrders.setStatus(status);

		//从wmsStockInOrderItems计算收货数量
		int receivedQuantity = wmsStockInOrderItems.stream().mapToInt(WmsStockInOrderItems::getReceivedQuantity).sum();

		//从wmsStockInOrderItems计算不良品数量
		int defectQuantity = wmsStockInOrderItems.stream().mapToInt(WmsStockInOrderItems::getDefectiveQuantity).sum();

		wmsStockInOrders.setTotalReceivedQuantity(receivedQuantity);
		wmsStockInOrders.setTotalDefectiveQuantity(defectQuantity);

		boolean b1 = updateById(wmsStockInOrders);

		return status;


	}

    /**
	 * 生成入库单号 ASN+8位年月日+4位序号
	 */
	public String generateOrderNumber() {
		//2025-09-05 00:00:00 --->20250905
		String time = DateUtils.now().substring(0, 10).replace("-", "");
		//key 每天产生一个key
		String key = "wms:asn_number"+time;
		long incr = 0;
		try {
			incr = redisUtil.incr(key, 1);
			//设置key的过期时间
			if(incr==1){
				redisUtil.expire(key, 24*60*60+30);
			}
		} catch (Exception e) {
			throw new JeecgBootException("生成入库单号出现异常");
		}
		return "ASN"+time+String.format("%04d", incr);

	}
}
