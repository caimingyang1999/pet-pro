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
 * 疫苗记录表 pet_vaccine
 *
 * @author ruoyi
 */
@Data
@TableName("pet_vaccine")
public class PetVaccine implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宠物ID */
    @ApiModelProperty("宠物ID")
    private Long petId;

    /** 疫苗名称 */
    @ApiModelProperty("疫苗名称")
    private String vaccineName;

    /** 接种日期 */
    @ApiModelProperty("接种日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date inoculationDate;

    /** 下次接种日期 */
    @ApiModelProperty("下次接种日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date nextDate;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
