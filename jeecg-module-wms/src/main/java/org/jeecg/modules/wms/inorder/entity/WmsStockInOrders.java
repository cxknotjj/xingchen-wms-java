package org.jeecg.modules.wms.inorder.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: 入库单主表
 * @Author: jeecg-boot
 * @Date:   2026-07-24
 * @Version: V1.0
 */
@Schema(description="入库单主表")
@Data
@TableName("wms_stock_in_orders")
public class WmsStockInOrders implements Serializable {
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
	/**入库单号*/
	@Excel(name = "入库单号", width = 15)
    @Schema(description = "入库单号")
    private java.lang.String orderNumber;
	/**入库类型*/
	@Excel(name = "入库类型", width = 15, dicCode = "asn_type")
    @Dict(dicCode = "asn_type")
    @Schema(description = "入库类型")
    private java.lang.String orderType;
	/**来源单号*/
	@Excel(name = "来源单号", width = 15)
    @Schema(description = "来源单号")
    private java.lang.String sourceNumber;
	/**货主id*/
	@Excel(name = "货主id", width = 15)
    @Schema(description = "货主id")
    private java.lang.String ownerId;
	/**预计到货时间*/
	@Excel(name = "预计到货时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预计到货时间")
    private java.util.Date expectedArrivalTime;
	/**状态*/
	@Excel(name = "状态", width = 15, dicCode = "asn_status")
    @Dict(dicCode = "asn_status")
    @Schema(description = "状态")
    private java.lang.String status;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private java.lang.String remarks;
	/**预期总数量(冗余字段)*/
	@Excel(name = "预期总数量(冗余字段)", width = 15)
    @Schema(description = "预期总数量(冗余字段)")
    private java.lang.Integer totalExpectedQuantity;
	/**实际收货总量*/
	@Excel(name = "实际收货总量", width = 15)
    @Schema(description = "实际收货总量")
    private java.lang.Integer totalReceivedQuantity;
	/**已上架总量*/
	@Excel(name = "已上架总量", width = 15)
    @Schema(description = "已上架总量")
    private java.lang.Integer totalShelvedQuantity;
	/**不良品总数量(冗余字段)*/
	@Excel(name = "不良品总数量(冗余字段)", width = 15)
    @Schema(description = "不良品总数量(冗余字段)")
    private java.lang.Integer totalDefectiveQuantity;
	/**仓库*/
	@Excel(name = "仓库", width = 15)
    @Schema(description = "仓库")
    private java.lang.String warehouseId;
    /**货主姓名*/
    @Schema(description = "货主姓名")
    @TableField(exist = false)
    private java.lang.String ownerName;
}
