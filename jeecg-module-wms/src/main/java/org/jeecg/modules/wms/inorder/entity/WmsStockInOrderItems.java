package org.jeecg.modules.wms.inorder.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.UnsupportedEncodingException;

/**
 * @Description: 入库单明细
 * @Author: jeecg-boot
 * @Date:   2026-07-24
 * @Version: V1.0
 */
@Schema(description="入库单明细")
@Data
@TableName("wms_stock_in_order_items")
public class WmsStockInOrderItems implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
	/**创建人*/
    @Schema(description = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @Schema(description = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
	/**入库单id*/
    @Schema(description = "入库单id")
    private java.lang.String orderId;
	/**商品id*/
	@Excel(name = "商品id", width = 15)
    @Schema(description = "商品id")
    private java.lang.String productId;
	/**采购数量*/
	@Excel(name = "采购数量", width = 15)
    @Schema(description = "采购数量")
    private java.lang.Integer expectedQuantity;
	/**实际收货数量*/
	@Excel(name = "实际收货数量", width = 15)
    @Schema(description = "实际收货数量")
    private java.lang.Integer receivedQuantity;
	/**上架数量*/
	@Excel(name = "上架数量", width = 15)
    @Schema(description = "上架数量")
    private java.lang.Integer shelvedQuantity;
	/**不良品数量*/
	@Excel(name = "不良品数量", width = 15)
    @Schema(description = "不良品数量")
    private java.lang.Integer defectiveQuantity;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private java.lang.String remarks;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @Schema(description = "状态")
    private java.lang.String status;

    @Schema(description = "商品名称")
    @TableField(exist = false)
    private String productName;

    @Schema(description = "商品编码")
    @TableField(exist = false)
    private String productCode;
}
