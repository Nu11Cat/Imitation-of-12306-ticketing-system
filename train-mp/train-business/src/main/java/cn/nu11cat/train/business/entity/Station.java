package cn.nu11cat.train.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 车站
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Getter
@Setter
@TableName("station")
public class Station implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 站名
     */
    private String name;

    /**
     * 站名拼音
     */
    private String namePinyin;

    /**
     * 站名拼音首字母
     */
    private String namePy;

    /**
     * 新增时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
