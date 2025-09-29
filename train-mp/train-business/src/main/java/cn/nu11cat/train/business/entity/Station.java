package cn.nu11cat.train.business.entity;

import lombok.Data;

import java.util.Date;
/**
 * <p>
 * 车站
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Data
public class Station {
    private Long id;

    private String name;

    private String namePinyin;

    private String namePy;

    private Date createTime;

    private Date updateTime;

}