package cn.nu11cat.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRegisterReq {

    @NotBlank(message = "【手机号】不可为空")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

}
