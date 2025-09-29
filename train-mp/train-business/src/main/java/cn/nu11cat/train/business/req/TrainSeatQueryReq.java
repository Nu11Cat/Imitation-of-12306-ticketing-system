package cn.nu11cat.train.business.req;

import cn.nu11cat.train.common.req.PageReq;
import lombok.Data;

@Data
public class TrainSeatQueryReq extends PageReq {

    private String trainCode;

}
