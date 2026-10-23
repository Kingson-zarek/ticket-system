package com.hnty.ticketsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnty.ticketsystem.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Crown
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
