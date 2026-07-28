package org.jeecg.modules.wms.inventory.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 商品导入模型类
 * @Author: Mr.M
 * @Date:   2025-08-05
 * @Version: V1.0
 */
@Data
public class WmsInventoryImport implements Serializable {
    private static final long serialVersionUID = 1L;
    /**储位编码*/
    @ExcelProperty("储位编码")
    @Schema(description = "储位编码")
    private java.lang.String locationCode;

    /**在库数量*/
    @ExcelProperty("在库数量")
    @Schema(description = "在库数量")
    private java.lang.Integer stockQuantity;

    /**分配数量*/
    @ExcelProperty("分配数量")
    @Schema(description = "分配数量")
    private java.lang.Integer allocatedQuantity;

    /**可用数量*/
    @ExcelProperty("可用数量")
    @Schema(description = "可用数量")
    private java.lang.Integer availableQuantity;

    /**批号 */
    @ExcelProperty("批号")
    @Schema(description = "批号 ")
    private java.lang.String batchNumber;

    /**入库时间*/
    @ExcelProperty("入库时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入库时间")
    private java.util.Date stockInTime;

    /**保质期到期日*/
    @ExcelProperty("保质期到期日")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @Schema(description = "保质期到期日")
    private java.util.Date expiryDate;

    /**货主*/
    @ExcelProperty("货主")
    @Schema(description = "货主")
    private java.lang.String ownerId;

    /**商品编码*/
    @ExcelProperty("商品编码")
    @Schema(description = "商品编码")
    @TableField(exist = false)
    private java.lang.String productCode;

    /**商品名称*/
    @ExcelProperty("商品")
    @Schema(description = "商品名称")
    @TableField(exist = false)
    private java.lang.String productName;

    /**货主编码*/
    @ExcelProperty("货主编码")
    @Schema(description = "货主编码")
    @TableField(exist = false)
    private java.lang.String ownerCode;

    /**货主名称*/
    @ExcelProperty("货主")
    @Schema(description = "货主名称")
    @TableField(exist = false)
    private java.lang.String ownerName;

    /**仓库名称*/
    @ExcelProperty("仓库名称")
    @Schema(description = "仓库名称")
    @TableField(exist = false)
    private java.lang.String warehouseName;
}
