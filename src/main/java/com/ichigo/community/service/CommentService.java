package com.ichigo.community.service;

import com.ichigo.community.entity.Comment;
import com.ichigo.community.mapper.CommentMapper;
import com.ichigo.community.util.CommunityConstant;
import com.ichigo.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 分页查找评论数据
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 统计评论数
     * @param entityType
     * @param entityId
     * @return
     */
    public int findCommentCount(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论，之后更新帖子的评论数，由事务统一管理
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        //判空
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //添加评论
        //转换HTML标签
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //敏感词过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);

        //更新帖子评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //获取帖子评论数
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //更新帖子评论数
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }
}
