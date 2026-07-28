package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import me.zhyd.oauth.utils.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import org.jeecg.modules.wms.goods.entity.WmsProductBrand;
import org.jeecg.modules.wms.goods.entity.WmsProductCategories;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.excel.WmsProductsImport;
import org.jeecg.modules.wms.goods.mapper.WmsProductBrandMapper;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.jeecg.modules.wms.goods.service.IWmsProductBrandService;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.excel.WmsInventoryImport;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IWmsCargoOwnersService wmsCargoOwnersService;
    @Autowired
    private IWmsWarehousesService wmsWarehousesService;
    @Autowired
    private IWmsProductsService wmsProductsService;

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

    @Override
    public void importInventory(List<WmsInventoryImport> importInventoryList) {
        // 将WmsInventoryImport对象转换成wmsInventory对象
        // 遍历inventoryImport
        for (int i = 0; i < importInventoryList.size(); i++) {
            WmsInventoryImport wmsInventoryImport = importInventoryList.get(i);

            // 必填字段校验
            if (StringUtils.isEmpty(wmsInventoryImport.getWarehouseName())
                    || StringUtils.isEmpty(wmsInventoryImport.getLocationCode())
                    || StringUtils.isEmpty(wmsInventoryImport.getProductCode())
                    || StringUtils.isEmpty(wmsInventoryImport.getProductName())
                    || wmsInventoryImport.getAllocatedQuantity() == null
                    || StringUtils.isEmpty(wmsInventoryImport.getOwnerCode())
                    || StringUtils.isEmpty(wmsInventoryImport.getOwnerName())
                    || wmsInventoryImport.getAvailableQuantity() == null
                    || wmsInventoryImport.getStockQuantity() == null
            ) {
                throw new RuntimeException("仓库名称、储位编码、商品编码、货主编码、货主名称、可用数量、库存数量不能为空");
            }
            
            WmsInventory wmsInventory = convertWmsInventory(wmsInventoryImport);
            try{
                add(wmsInventory);
            }catch (Exception e) {
                throw new RuntimeException(wmsInventoryImport.getLocationCode()+"导入失败");
            }

        }
    }

    public void add (WmsInventory wmsInventory) {
        // 在插入之前查询是否存在相同唯一键的库存
        WmsInventory existingInventory = getInventoryByUniqueKey(wmsInventory.getProductId(), wmsInventory.getLocationCode(), wmsInventory.getBatchNumber());
        if (existingInventory != null) {
            throw new JeecgBootException("储位编码"+wmsInventory.getLocationCode()+"已存在");
        }
        // 插入库存
        save(wmsInventory);
    }

    /**
     * 将wmsInventoryImport转换成WmsInventory
     */
    public WmsInventory convertWmsInventory(WmsInventoryImport wmsInventoryImport) {
        WmsInventory wmsInventory = new WmsInventory();

        // 1. 直接复制的字段
        BeanUtils.copyProperties(wmsInventoryImport, wmsInventory);

        // 2. 通过名称查询获取 ID（关键转换）
        //    - 商品编码 → 商品ID
        LambdaQueryWrapper<WmsProducts> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(WmsProducts::getProductCode, wmsInventoryImport.getProductCode());
        WmsProducts product = wmsProductsService.getOne(productQuery);
        if (product != null) {
            wmsInventory.setProductId(product.getId());
        }

        //    - 货主编码 → 货主ID
        LambdaQueryWrapper<WmsCargoOwners> ownerQuery = new LambdaQueryWrapper<>();
        ownerQuery.eq(WmsCargoOwners::getOwnerCode, wmsInventoryImport.getOwnerCode());
        WmsCargoOwners owner = wmsCargoOwnersService.getOne(ownerQuery);
        if (owner != null) {
            wmsInventory.setOwnerId(owner.getId());
        }

        //    - 仓库名称 → 仓库ID
        LambdaQueryWrapper<WmsWarehouses> warehouseQuery = new LambdaQueryWrapper<>();
        warehouseQuery.eq(WmsWarehouses::getWarehouseName, wmsInventoryImport.getWarehouseName());
        WmsWarehouses warehouse = wmsWarehousesService.getOne(warehouseQuery);
        if (warehouse != null) {
            wmsInventory.setWarehouseId(warehouse.getId());
        }


        if (wmsInventory.getIsSellable() == null) {
            wmsInventory.setIsSellable("1");  // 默认可售
        }



        return wmsInventory;
    }
}