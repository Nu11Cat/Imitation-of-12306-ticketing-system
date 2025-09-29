package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class TrainSeat {
    private Long id;

    private String trainCode;

    private Integer carriageIndex;

    @TableField("`row`")
    private String row;

    @TableField("`col`")
    private String col;

    private String seatType;

    private Integer carriageSeatIndex;

    private Date createTime;

    private Date updateTime;

}