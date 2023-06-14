package com.ichigo.community.service;

import com.ichigo.community.entity.DiscussPost;
import com.ichigo.community.mapper.DiscussPostMapper;
import com.ichigo.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 分页查找帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 获取帖子数
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 添加帖子
     * @param discussPost
     * @return
     */
    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //转义HTML标签
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 获取帖子详情
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 更新帖子评论数
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    /**
     * 更新帖子类型
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 更新帖子状态
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 更新帖子分数
     * @param id
     * @param score
     * @return
     */
    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }

}
