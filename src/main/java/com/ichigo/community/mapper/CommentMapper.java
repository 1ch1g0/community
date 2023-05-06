package com.ichigo.community.mapper;

import com.ichigo.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //分页查询评论信息
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    //获取评论数量
    int selectCountByEntity(int entityType, int entityId);

    //添加评论
    int insertComment(Comment comment);

    //根据id获取一个评论
    Comment selectCommentById(int id);
}
