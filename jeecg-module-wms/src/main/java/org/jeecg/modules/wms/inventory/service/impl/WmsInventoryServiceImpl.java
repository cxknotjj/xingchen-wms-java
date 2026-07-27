package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import me.zhyd.oauth.utils.StringUtils;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2026-07-26
 * @Version: V1.0
 */
@Service
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory> implements IWmsInventoryService {

    @Override
    public WmsInventory getInventoryByUniqueKey(String productId, String locationCode, String batchNumber) {
        // sql
        // select * from wms_inventory where product_id = ? and location_code = ? and batch_number = ?
        // select * from wms_inventory where product_id = ? and location_code = ? and batch_number is null
        LambdaQueryWrapper<WmsInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsInventory::getProductId, productId)
                .eq(WmsInventory::getLocationCode, locationCode)
                .eq(StringUtils.isNotEmpty(batchNumber), WmsInventory::getBatchNumber, batchNumber);
        return this.getOne(queryWrapper);
    }

    @Override
    public IPage<WmsInventory> queryInventoryList(WmsInventory wmsInventory, Integer pageNo, Integer pageSize) {
        Page<WmsInventory> page = PageHelper.startPage(pageNo, pageSize);
        List<WmsInventory> list = baseMapper.queryInventoryList(wmsInventory);
        PageDTO<WmsInventory> wmsInventoryPageDTO = new PageDTO<>();
        wmsInventoryPageDTO.setRecords(list);
        wmsInventoryPageDTO.setTotal(page.getTotal());
        wmsInventoryPageDTO.setSize(page.getPageSize());
        wmsInventoryPageDTO.setCurrent(page.getPageNum());
        wmsInventoryPageDTO.setPages(page.getPages());
        return wmsInventoryPageDTO;
    }
}