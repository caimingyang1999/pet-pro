package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 宠物返回对象
 *
 * @author ruoyi
 */
@Data
@ApiModel("宠物返回对象")
public class PetVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("宠物ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("所属用户名")
    private String userName;

    @ApiModelProperty("宠物名称")
    private String name;

    @ApiModelProperty("宠物头像")
    private String avatar;

    @ApiModelProperty("品种")
    private String breed;

    @ApiModelProperty("出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @ApiModelProperty("性别（0-母 1-公）")
    private String gender;

    @ApiModelProperty("体重(kg)")
    private Double weight;

    @ApiModelProperty("毛色")
    private String color;

    @ApiModelProperty("绝育状态（0-未绝育 1-已绝育）")
    private String sterilization;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("疫苗记录列表")
    private List<PetVaccineVO> vaccineList;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Data
    @ApiModel("疫苗记录")
    public static class PetVaccineVO implements Serializable
    {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("疫苗记录ID")
        private Long id;

        @ApiModelProperty("疫苗名称")
        private String vaccineName;

        @ApiModelProperty("接种日期")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date inoculationDate;

        @ApiModelProperty("下次接种日期")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date nextDate;
    }
}
