package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DailyTrainStation {
    private Long id;

    private Date date;

    private String trainCode;

    @TableField("`index`")
    private Integer index;

    private String name;

    private String namePinyin;

    private Date inTime;

    private Date outTime;

    private Date stopTime;

    private BigDecimal km;

    private Date createTime;

    private Date updateTime;

}