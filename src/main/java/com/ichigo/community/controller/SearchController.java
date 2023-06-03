package com.ichigo.community.controller;

import com.ichigo.community.entity.DiscussPost;
import com.ichigo.community.entity.Page;
import com.ichigo.community.service.ElasticsearchService;
import com.ichigo.community.service.LikeService;
import com.ichigo.community.service.UserService;
import com.ichigo.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 响应搜索帖子
     * @param keyword
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        //搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        //聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(searchResult != null){
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                //封装帖子信息
                map.put("post", post);
                //封装作者信息
                map.put("user", userService.findById(post.getUserId()));
                //封装点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                //聚合数据
                discussPosts.add(map);
            }
        }
        //将聚合数据和搜索词添加到模板中
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        //设置分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }

}
