package cn.nu11cat.train.business.req;

import cn.nu11cat.train.common.req.PageReq;
import lombok.Data;

@Data
public class DailyTrainSeatQueryReq extends PageReq {

    private String trainCode;

}
