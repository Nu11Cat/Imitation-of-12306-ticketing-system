package cn.nu11cat.train.member.req;

import cn.nu11cat.train.common.req.PageReq;
import lombok.Data;

@Data
public class TicketQueryReq extends PageReq {

    private Long memberId;

}
