package com.hnty.ticketsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnty.ticketsystem.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author Crown
 */
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    @Select("select max(ticket_no) from t_ticket where ticket_no like #{prefix}")
    String selectMaxNo(@Param("prefix") String prefix);

}
