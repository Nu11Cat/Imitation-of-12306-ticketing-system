package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 余票信息
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Data
public class DailyTrainTicket {
    private Long id;

    private Date date;

    private String trainCode;

    private String start;

    private String startPinyin;

    private Date startTime;

    private Integer startIndex;

    private String end;

    private String endPinyin;

    private Date endTime;

    private Integer endIndex;

    private Integer ydz;

    private BigDecimal ydzPrice;

    private Integer edz;

    private BigDecimal edzPrice;

    private Integer rw;

    private BigDecimal rwPrice;

    private Integer yw;

    private BigDecimal ywPrice;

    private Date createTime;

    private Date updateTime;

    @Version
    private Integer version; // 乐观锁字段

}