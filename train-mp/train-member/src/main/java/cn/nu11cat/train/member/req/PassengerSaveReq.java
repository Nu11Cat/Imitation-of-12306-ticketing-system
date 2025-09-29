package cn.nu11cat.train.member.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class PassengerSaveReq {
    private Long id;

    private Long memberId;

    @NotBlank(message = "【乘车人姓名】不可为空")
    private String name;

    @NotBlank(message = "【乘车人身份证信息】不可为空")
    private String idCard;

    @NotBlank(message = "【乘车人类型】不可为空")
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

}