package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 座位
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Getter
@Setter
@TableName("train_seat")
public class TrainSeat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 车次编号
     */
    private String trainCode;

    /**
     * 厢序
     */
    private Integer carriageIndex;

    /**
     * 排号|01, 02
     */
    @TableField("`row`")
    private String row;

    /**
     * 列号|枚举[SeatColEnum]
     */
    @TableField("`col`")
    private String col;

    /**
     * 座位类型|枚举[SeatTypeEnum]
     */
    private String seatType;

    /**
     * 同车厢座序
     */
    private Integer carriageSeatIndex;

    /**
     * 新增时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
