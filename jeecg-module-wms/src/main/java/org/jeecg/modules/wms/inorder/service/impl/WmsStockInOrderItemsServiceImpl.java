package org.jeecg.modules.wms.inorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.mapper.WmsStockInOrderItemsMapper;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 入库单明细
 * @Author: jeecg-boot
 * @Date:   2026-07-24
 * @Version: V1.0
 */
@Service
public class WmsStockInOrderItemsServiceImpl extends ServiceImpl<WmsStockInOrderItemsMapper, WmsStockInOrderItems> implements IWmsStockInOrderItemsService {
	
	@Autowired
	private WmsStockInOrderItemsMapper wmsStockInOrderItemsMapper;

	@Autowired
	private IWmsTasksRecordsService wmsTasksRecordsService;

	@Override
	public List<WmsStockInOrderItems> selectByMainId(String mainId) {
		return wmsStockInOrderItemsMapper.selectByMainId(mainId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateReceivedStatus(String stockInOrderItemId) {
		// 更新入库单明细中的收货数量及不良品数量，当不良品数量加良品数量等于采购数量，更新状态为收货完成。
		// 查询入库单明细
		WmsStockInOrderItems stockInOrderItems = wmsStockInOrderItemsMapper.selectById(stockInOrderItemId);

		// 根据入库单明细id找到关联的收货记录，拿到其中的收货数量和不良品数量
		LambdaQueryWrapper<WmsTasksRecords> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(WmsTasksRecords::getStockInOrderItemId, stockInOrderItemId);
		List<WmsTasksRecords> recordsList = wmsTasksRecordsService.list(queryWrapper);

		// 良品数量
		int goodProductSum = recordsList.stream()
				.filter(taskRecords -> taskRecords.getInventoryAttribute().equals(WarehouseDictEnum.RECEIVING_GOOD.getCode()))
				.mapToInt(WmsTasksRecords::getExecQuantity)
				.sum();
		// 不良品数量
		int defectProductSum = recordsList.stream()
				.filter(taskRecords -> taskRecords.getInventoryAttribute().equals(WarehouseDictEnum.RECEIVING_DEFECTIVE.getCode()))
				.mapToInt(WmsTasksRecords::getExecQuantity)
				.sum();
		// 更新良品数量
		if (goodProductSum > 0) {
			stockInOrderItems.setReceivedQuantity(goodProductSum);
		}
		// 更新不良品数量
		if (defectProductSum > 0) {
			stockInOrderItems.setDefectiveQuantity(defectProductSum);
		}

		// 当良品数量加不良品数量等于采购数量，更新状态为收获完成
		if (goodProductSum + defectProductSum == stockInOrderItems.getExpectedQuantity()) {
			stockInOrderItems.setStatus(WarehouseDictEnum.INBOUND_DETAIL_RECEIVED.getCode());
		}
		// 更新入库单明细
		boolean b = this.updateById(stockInOrderItems);
		if (!b) {
			throw new RuntimeException("更新入库单明细失败");
		}
	}
}
