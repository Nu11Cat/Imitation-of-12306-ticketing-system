package cn.nu11cat.train.business.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmOrderTicketReq {

    /**
     * 乘客ID
     */
    @NotNull(message = "【乘客ID】不能为空")
    private Long passengerId;

    /**
     * 乘客票种
     */
    @NotBlank(message = "【乘客票种】不能为空")
    private String passengerType;

    /**
     * 乘客名称
     */
    @NotBlank(message = "【乘客名称】不能为空")
    private String passengerName;

    /**
     * 乘客身份证
     */
    @NotBlank(message = "【乘客身份证】不能为空")
    private String passengerIdCard;

    /**
     * 座位类型code
     */
    @NotBlank(message = "【座位类型code】不能为空")
    private String seatTypeCode;

    /**
     * 选座，可空，值示例：A1
     */
    private String seat;

}
