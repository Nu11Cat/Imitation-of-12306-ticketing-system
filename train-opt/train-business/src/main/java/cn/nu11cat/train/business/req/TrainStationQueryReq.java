package cn.nu11cat.train.business.req;

import cn.nu11cat.train.common.req.PageReq;
import lombok.Data;

@Data
public class TrainStationQueryReq extends PageReq {

    private String trainCode;

}
