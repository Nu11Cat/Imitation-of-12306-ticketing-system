package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
/**
 * <p>
 * 火车车站
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Data
public class TrainStation {
    private Long id;

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