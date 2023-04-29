package com.ichigo.community.controller;

import com.ichigo.community.entity.Comment;
import com.ichigo.community.entity.DiscussPost;
import com.ichigo.community.entity.Page;
import com.ichigo.community.entity.User;
import com.ichigo.community.service.CommentService;
import com.ichigo.community.service.DiscussPostService;
import com.ichigo.community.service.UserService;
import com.ichigo.community.util.CommunityConstant;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    /**
     * 响应新增帖子
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403, "您还没有登录哦！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 响应查看帖子详情(包括分页评论)
     * @param discussPostId
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //获取帖子信息
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //获取作者信息
        User user = userService.findById(post.getUserId());
        model.addAttribute(user);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论VO列表(view object，想要显示发布者的昵称而不是id)
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                //评论VO
                Map<String, Object> commentVo = new HashMap<>();
                //评论内容
                commentVo.put("comment", comment);
                //评论发布者
                commentVo.put("user", userService.findById(comment.getUserId()));

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for (Comment reply : replyList){
                        //回复VO
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复内容
                        replyVo.put("reply", reply);
                        //回复发布者
                        replyVo.put("user", userService.findById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findById(reply.getTargetId());
                        replyVo.put("target", target);

                        //将回复VO添加到回复VO列表中
                        replyVoList.add(replyVo);
                    }
                }
                //将回复VO列表添加到评论VO中
                commentVo.put("replys", replyVoList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                //添加回复数量
                commentVo.put("replyCount", replyCount);

                //将评论VO添加到评论VO列表中
                commentVoList.add(commentVo);
            }
        }

        //将评论信息传入thymeleaf模板
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
}
