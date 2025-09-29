package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
/**
 * <p>
 * 每日车厢
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Data
public class DailyTrainCarriage {
    private Long id;

    private Date date;

    private String trainCode;

    @TableField("`index`")
    private Integer index;

    private String seatType;

    private Integer seatCount;

    private Integer rowCount;

    private Integer colCount;

    private Date createTime;

    private Date updateTime;

}