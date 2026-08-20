package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 宠物信息表 pet_info
 *
 * @author ruoyi
 */
@Data
@TableName("pet_info")
public class PetInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（关联sys_user） */
    @ApiModelProperty("用户ID（关联sys_user）")
    private Long userId;

    /** 宠物名称 */
    @ApiModelProperty("宠物名称")
    private String name;

    /** 宠物头像 */
    @ApiModelProperty("宠物头像")
    private String avatar;

    /** 品种 */
    @ApiModelProperty("品种")
    private String breed;

    /** 出生日期 */
    @ApiModelProperty("出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 性别（0-母 1-公） */
    @ApiModelProperty("性别（0-母 1-公）")
    private String gender;

    /** 体重(kg) */
    @ApiModelProperty("体重(kg)")
    private Double weight;

    /** 毛色 */
    @ApiModelProperty("毛色")
    private String color;

    /** 绝育状态（0-未绝育 1-已绝育） */
    @ApiModelProperty("绝育状态（0-未绝育 1-已绝育）")
    private String sterilization;

    /** 备注 */
    @ApiModelProperty("备注")
    private String remark;

    /** 删除标志（0-存在 1-删除） */
    @ApiModelProperty("删除标志（0-存在 1-删除）")
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

    /** 疫苗记录列表 */
    @ApiModelProperty("疫苗记录列表")
    @TableField(exist = false)
    private List<PetVaccine> vaccineList;

    /** 所属用户名（关联查询） */
    @ApiModelProperty("所属用户名")
    @TableField(exist = false)
    private String userName;
}
