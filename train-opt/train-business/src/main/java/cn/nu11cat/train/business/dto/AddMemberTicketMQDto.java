package cn.nu11cat.train.business.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AddMemberTicketMQDto {
    private Long confirmOrderId;
    private Long memberId;
    private List<PassengerTicketInfo> tickets;
    private Date trainDate;
    private String trainCode;
    private String logId;

    @Data
    public static class PassengerTicketInfo {
        private Long passengerId;
        private String passengerName;
        private Integer carriageIndex;
        private String rowIndex;
        private String colIndex;
        private String seatType;
        private String startStation;
        private Date startTime;
        private String endStation;
        private Date endTime;

    }
}
