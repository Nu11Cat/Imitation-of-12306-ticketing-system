package cn.nu11cat.train.member.req;

import cn.nu11cat.train.common.req.PageReq;
import lombok.Data;

@Data
public class PassengerQueryReq extends PageReq {

    private Long memberId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

}