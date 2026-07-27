package org.jeecg.modules.wms.inorder.service;

import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 入库单明细
 * @Author: jeecg-boot
 * @Date:   2026-07-24
 * @Version: V1.0
 */
public interface IWmsStockInOrderItemsService extends IService<WmsStockInOrderItems> {

	/**
	 * 通过主表id查询子表数据
	 *
	 * @param mainId 主表id
	 * @return List<WmsStockInOrderItems>
	 */
	public List<WmsStockInOrderItems> selectByMainId(String mainId);

	/**
	 * 更新入库单明细中的收获数量
	 */
	public void updateReceivedStatus(String stockInOrderItemId);

	/**
	 * 更新入库单明细中的上架数量
	 */
	public void updateShelfStatus(String stockInOrderItemId);
}