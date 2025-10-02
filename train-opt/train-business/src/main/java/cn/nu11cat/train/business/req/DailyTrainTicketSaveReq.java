package cn.nu11cat.train.business.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DailyTrainTicketSaveReq {

    /**
     * id
     */
    private Long id;

    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @NotNull(message = "【日期】不能为空")
    private Date date;

    /**
     * 车次编号
     */
    @NotBlank(message = "【车次编号】不能为空")
    private String trainCode;

    /**
     * 出发站
     */
    @NotBlank(message = "【出发站】不能为空")
    private String start;

    /**
     * 出发站拼音
     */
    @NotBlank(message = "【出发站拼音】不能为空")
    private String startPinyin;

    /**
     * 出发时间
     */
    @JsonFormat(pattern = "HH:mm:ss",timezone = "GMT+8")
    @NotNull(message = "【出发时间】不能为空")
    private Date startTime;

    /**
     * 出发站序|本站是整个车次的第几站
     */
    @NotNull(message = "【出发站序】不能为空")
    private Integer startIndex;

    /**
     * 到达站
     */
    @NotBlank(message = "【到达站】不能为空")
    private String end;

    /**
     * 到达站拼音
     */
    @NotBlank(message = "【到达站拼音】不能为空")
    private String endPinyin;

    /**
     * 到站时间
     */
    @JsonFormat(pattern = "HH:mm:ss",timezone = "GMT+8")
    @NotNull(message = "【到站时间】不能为空")
    private Date endTime;

    /**
     * 到站站序|本站是整个车次的第几站
     */
    @NotNull(message = "【到站站序】不能为空")
    private Integer endIndex;

    /**
     * 一等座余票
     */
    @NotNull(message = "【一等座余票】不能为空")
    private Integer ydz;

    /**
     * 一等座票价
     */
    @NotNull(message = "【一等座票价】不能为空")
    private BigDecimal ydzPrice;

    /**
     * 二等座余票
     */
    @NotNull(message = "【二等座余票】不能为空")
    private Integer edz;

    /**
     * 二等座票价
     */
    @NotNull(message = "【二等座票价】不能为空")
    private BigDecimal edzPrice;

    /**
     * 软卧余票
     */
    @NotNull(message = "【软卧余票】不能为空")
    private Integer rw;

    /**
     * 软卧票价
     */
    @NotNull(message = "【软卧票价】不能为空")
    private BigDecimal rwPrice;

    /**
     * 硬卧余票
     */
    @NotNull(message = "【硬卧余票】不能为空")
    private Integer yw;

    /**
     * 硬卧票价
     */
    @NotNull(message = "【硬卧票价】不能为空")
    private BigDecimal ywPrice;

    /**
     * 新增时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

}
