package com.ichigo.community.controller;

import com.ichigo.community.entity.Page;
import com.ichigo.community.entity.User;
import com.ichigo.community.service.FollowService;
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

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 响应关注请求
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        //获取当前用户信息
        User user = hostHolder.getUser();
        //关注
        followService.follow(user.getId(), entityType, entityId);
        //返回成功信息
        return CommunityUtil.getJSONString(0, "关注成功！");
    }

    /**
     * 响应取关请求
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        //获取当前用户信息
        User user = hostHolder.getUser();
        //取关
        followService.unfollow(user.getId(), entityType, entityId);
        //返回成功信息
        return CommunityUtil.getJSONString(0, "取关成功！");
    }

    /**
     * 响应查看用户关注列表请求
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        //获取被查看用户的信息
        User user = userService.findById(userId);
        //判空
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        //将该用户信息添加到模板
        model.addAttribute("user", user);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        //获取该用户的关注列表
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        //判空，封装对于列表中的每个用户，当前用户是否已关注，用于按钮状态判断
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        //将封装好的关注列表添加到模板中
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    /**
     * 响应查看用户粉丝列表请求
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        //获取被查看用户的信息
        User user = userService.findById(userId);
        //判空
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        //将该用户信息添加到模板
        model.addAttribute("user", user);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        //获取该用户的粉丝列表
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        //判空，封装对于列表中的每个用户，当前用户是否已关注，用于按钮状态判断
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        //将封装好的关注列表添加到模板中
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    /**
     * 判断用户是否被当前用户关注
     * @param userId
     * @return
     */
    private boolean hasFollowed(int userId){
        //没登陆，直接返回false
        if(hostHolder.getUser() == null){
            return false;
        }
        //登陆了，查询是否已关注
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
