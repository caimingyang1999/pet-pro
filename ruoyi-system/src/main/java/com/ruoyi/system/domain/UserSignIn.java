package com.ruoyi.system.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 签到记录表 user_sign_in
 *
 * @author ruoyi
 */
@Data
@TableName("user_sign_in")
public class UserSignIn implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /**
     * 签到日期
     *
     * 数据库列类型为 DATE，这里必须用 LocalDate 而非 java.util.Date：
     * java.util.Date 是"时间点"，JDBC 会按连接时区（serverTimezone）做换算，
     * 零点会被换算成前一天 16:00（东八区 vs UTC），写入 DATE 列后被截断成前一天，
     * 而查询时的等值比较又带上了时分秒，导致"插入成功但查不到、再点就主键冲突"。
     * LocalDate 按纯日期绑定，没有任何时区换算，与 DATE 列语义一致。
     */
    @ApiModelProperty("签到日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signDate;

    /** 签到奖励积分 */
    @ApiModelProperty("签到奖励积分")
    private Integer pointsReward;

    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
