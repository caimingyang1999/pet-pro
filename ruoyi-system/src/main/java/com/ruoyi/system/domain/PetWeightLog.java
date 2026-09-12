package com.ruoyi.system.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 宠物体重记录表 pet_weight_log
 *
 * @author ruoyi
 */
@Data
@TableName("pet_weight_log")
public class PetWeightLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宠物ID */
    @ApiModelProperty("宠物ID")
    private Long petId;

    /** 用户ID（冗余存储，便于按用户校验归属） */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 体重(kg) */
    @ApiModelProperty("体重(kg)")
    private BigDecimal weight;

    /** 记录日期 */
    @ApiModelProperty("记录日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    /**
     * 数据库列为 DATE 类型，使用 LocalDate 而非 java.util.Date：
     * java.util.Date 是时间点，JDBC 会按连接时区做换算，跨时区时会整体偏移一天。
     */
    private LocalDate recordDate;

    /** 备注 */
    @ApiModelProperty("备注")
    private String remark;

    /** 删除标志（0-存在 1-删除） */
    @ApiModelProperty("删除标志")
    @TableLogic
    private String delFlag;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
