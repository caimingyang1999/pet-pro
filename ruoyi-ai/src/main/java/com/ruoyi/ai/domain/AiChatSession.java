package com.ruoyi.ai.domain;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI对话会话 ai_chat_session
 *
 * @author ruoyi
 */
@Data
@TableName("ai_chat_session")
@ApiModel("AI对话会话")
public class AiChatSession implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 会话标题 */
    @ApiModelProperty("会话标题")
    private String sessionTitle;

    /** 会话类型（general-通用咨询 pet_care-宠物护理 pet_medical-宠物医疗 pet_diet-宠物饮食 pet_behavior-宠物行为） */
    @ApiModelProperty("会话类型")
    private String sessionType;

    /** 使用的AI模型名称 */
    @ApiModelProperty("使用的AI模型名称")
    private String model;

    /** 消息总数 */
    @ApiModelProperty("消息总数")
    private Integer messageCount;

    /** 会话状态（1-正常 0-已删除） */
    @ApiModelProperty("会话状态")
    private String status;

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

    /** 用户昵称（关联查询，非数据库字段） */
    @ApiModelProperty("用户昵称")
    @TableField(exist = false)
    private String nickName;
}
