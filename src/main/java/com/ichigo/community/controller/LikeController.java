package com.ichigo.community.controller;

import com.ichigo.community.entity.User;
import com.ichigo.community.service.LikeService;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    /**
     * 响应点赞请求
     * 使用异步请求进行点赞，并异步刷新点赞数据
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId){
        //获取当前点赞用户数据
        User user = hostHolder.getUser();

        //点赞
        likeService.like(user.getId(), entityType, entityId);

        //获取点赞数
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //获取点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //将点赞数和点赞状态封装到map集合中
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //将封装的map集合返回给前端
        return CommunityUtil.getJSONString(0, null, map);
    }

}
