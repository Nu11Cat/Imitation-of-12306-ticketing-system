package cn.nu11cat.train.business.mapper.cust;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface DailyTrainTicketMapperCust {

    void updateCountBySell(@Param("date") Date date,
                           @Param("trainCode") String trainCode,
                           @Param("seatTypeCode") String seatTypeCode,
                           @Param("minStartIndex") Integer minStartIndex,
                           @Param("maxStartIndex") Integer maxStartIndex,
                           @Param("minEndIndex") Integer minEndIndex,
                           @Param("maxEndIndex") Integer maxEndIndex);
}
