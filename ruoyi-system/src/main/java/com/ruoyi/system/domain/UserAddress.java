package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户收货地址表 user_address
 *
 * @author ruoyi
 */
@Data
@TableName("user_address")
public class UserAddress implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 收件人 */
    @ApiModelProperty("收件人")
    private String receiverName;

    /** 联系电话 */
    @ApiModelProperty("联系电话")
    private String receiverPhone;

    /** 省份 */
    @ApiModelProperty("省份")
    private String province;

    /** 城市 */
    @ApiModelProperty("城市")
    private String city;

    /** 区县 */
    @ApiModelProperty("区县")
    private String district;

    /** 详细地址 */
    @ApiModelProperty("详细地址")
    private String detailAddress;

    /** 是否默认（0-否 1-是） */
    @ApiModelProperty("是否默认（0-否 1-是）")
    private String isDefault;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
