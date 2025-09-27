package cn.nu11cat.train.business.req;

import cn.nu11cat.train.common.req.PageReq;

public class TrainStationQueryReq extends PageReq {

    private String trainCode;

    @Override
    public String toString() {
        return "TrainStationQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                '}';
    }

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

}
