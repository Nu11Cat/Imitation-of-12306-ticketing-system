package cn.nu11cat.train.business.mapper;

import cn.nu11cat.train.business.entity.DailyTrainTicket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 余票信息 Mapper 接口
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface DailyTrainTicketMapper extends BaseMapper<DailyTrainTicket> {

    @Select("SELECT DISTINCT train_code FROM daily_train_ticket")
    List<String> selectAllTrainCodes();

}