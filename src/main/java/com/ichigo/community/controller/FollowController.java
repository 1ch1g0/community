package com.ichigo.community.controller;

import com.ichigo.community.entity.User;
import com.ichigo.community.service.FollowService;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

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

}
