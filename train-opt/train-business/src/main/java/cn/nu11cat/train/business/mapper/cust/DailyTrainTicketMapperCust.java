package cn.nu11cat.train.business.mapper.cust;

import cn.nu11cat.train.business.entity.DailyTrainTicket;
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

    void updateCountBySellOptimistic(@Param("date") Date date,
                                     @Param("trainCode") String trainCode,
                                     @Param("seatTypeCode") String seatTypeCode,
                                     @Param("minStartIndex") Integer minStartIndex,
                                     @Param("maxStartIndex") Integer maxStartIndex,
                                     @Param("minEndIndex") Integer minEndIndex,
                                     @Param("maxEndIndex") Integer maxEndIndex,
                                     @Param("version") Integer version);

    DailyTrainTicket selectByUnique(@Param("date") Date date,
                                    @Param("trainCode") String trainCode,
                                    @Param("start") String start,
                                    @Param("end") String end);
}
