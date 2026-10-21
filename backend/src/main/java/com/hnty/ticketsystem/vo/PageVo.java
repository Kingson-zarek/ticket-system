package com.hnty.ticketsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页返回体
 * 不直接返回 MyBatis-Plus 的 Page，那玩意会带一堆内部字段出去
 *
 * @param <T> 当前页的数据类型
 * @author Crown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVo<T> {

    private long total;

    private List<T> records;

    public static <T> PageVo<T> of(long total, List<T> records) {
        return new PageVo<>(total, records);
    }
}
