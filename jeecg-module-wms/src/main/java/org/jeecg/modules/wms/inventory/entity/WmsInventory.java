package org.jeecg.modules.wms.inventory.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.ExcelIgnore;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2026-07-26
 * @Version: V1.0
 */
@Data
@TableName("wms_inventory")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="库存表")
public class WmsInventory implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    @ExcelIgnore
    private java.lang.String id;
	/**创建人*/
    @Schema(description = "创建人")
    @ExcelIgnore
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    @ExcelIgnore
    private java.util.Date createTime;
	/**更新人*/
    @Schema(description = "更新人")
    @ExcelIgnore
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    @ExcelIgnore
    private java.util.Date updateTime;
	/**所属部门*/
    @Schema(description = "所属部门")
    @ExcelIgnore
    private java.lang.String sysOrgCode;
	/**商品id*/
    @Schema(description = "商品id")
    @ExcelIgnore
    private java.lang.String productId;
	/**储位编码*/
	@ExcelProperty("储位编码")
    @Schema(description = "储位编码")
    private java.lang.String locationCode;
	/**容器编码*/
    @Schema(description = "容器编码")
    @ExcelIgnore
    private java.lang.String containerCode;
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
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入库时间")
    private java.util.Date stockInTime;
	/**保质期到期日*/
	@ExcelProperty("保质期到期日")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "保质期到期日")
    private java.util.Date expiryDate;
	/**货主*/
	@ExcelProperty("货主")
    @Schema(description = "货主")
    private java.lang.String ownerId;
	/**是否可售*/
    @Schema(description = "是否可售")
    @ExcelIgnore
    private java.lang.String isSellable;
	/**仓库id*/
    @Schema(description = "仓库id")
    @ExcelIgnore
    private java.lang.String warehouseId;

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

	/**储位类型*/
    @Schema(description = "储位类型")
    @TableField(exist = false)
    @ExcelIgnore
    private java.lang.String locationType;

	/**储区类型*/
    @Schema(description = "储区类型")
    @TableField(exist = false)
    @ExcelIgnore
    private java.lang.String zoneType;

	/**储区名称*/
    @Schema(description = "储区名称")
    @TableField(exist = false)
    @ExcelIgnore
    private java.lang.String zoneName;
}