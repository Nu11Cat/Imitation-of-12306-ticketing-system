package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class TrainCarriage {
    private Long id;

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