package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.util.Date;

@Data
public class DailyTrainSeat {
    private Long id;

    private Date date;

    private String trainCode;

    private Integer carriageIndex;

//    @TableField("`row`")
//    private String row;
//
//    @TableField("`col`")
//    private String col;

//    @TableField("row_index")
    private String rowIndex;

//    @TableField("col_index")
    private String colIndex;


    private String seatType;

    private Integer carriageSeatIndex;

    private String sell;

    private Date createTime;

    private Date updateTime;

    @Version
    private Integer version; // 乐观锁字段

}