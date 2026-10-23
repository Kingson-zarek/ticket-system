package com.hnty.ticketsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnty.ticketsystem.entity.Ticket;
import com.hnty.ticketsystem.vo.ReportSummaryVo;
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

    /**
     * 首页看板的汇总数字
     * 自定义 SQL 不会自动拼逻辑删除条件，deleted = 0 要手写
     *
     * @return 各状态数量、今日新增和平均处理小时数
     */
    @Select("""
            select
                count(case when status = 'OPEN' then 1 end) as `open`,
                count(case when status = 'PROCESSING' then 1 end) as processing,
                count(case when status = 'RESOLVED' then 1 end) as resolved,
                count(case when status = 'CLOSED' then 1 end) as closed,
                count(case when created_at >= curdate() then 1 end) as today_new,
                coalesce(round(avg(case when resolved_at is not null
                    then timestampdiff(second, created_at, resolved_at) / 3600.0 end), 1), 0) as avg_handle_hours
            from t_ticket
            where deleted = 0
            """)
    ReportSummaryVo selectSummary();

}
