package cn.nu11cat.train.business.entity;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 每日车次
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Data
public class DailyTrain {
    private Long id;

    private Date date;

    private String code;

    private String type;

    private String start;

    private String startPinyin;

    private Date startTime;

    private String end;

    private String endPinyin;

    private Date endTime;

    private Date createTime;

    private Date updateTime;

}