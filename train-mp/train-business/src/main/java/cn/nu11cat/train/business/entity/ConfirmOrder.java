package cn.nu11cat.train.business.entity;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 确认订单
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Data
public class ConfirmOrder {
    private Long id;

    private Long memberId;

    private Date date;

    private String trainCode;

    private String start;

    private String end;

    private Long dailyTrainTicketId;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String tickets;

}