package com.ichigo.community.service;

import com.ichigo.community.entity.DiscussPost;
import com.ichigo.community.mapper.elasticsearch.DiscussPostRespository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRespository discussRespository;

    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;

    /**
     * 添加功能
     * @param post
     */
    public void saveDiscussPost(DiscussPost post){
        discussRespository.save(post);
    }

    /**
     * 删除功能
     * @param id
     */
    public void deleteDiscussPost(int id){
        discussRespository.deleteById(id);
    }

    /**
     * 搜索功能
     * @param keyword
     * @param current
     * @param limit
     * @return
     */
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit){
        //高亮条件
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")     //给哪些自动设置高亮
                .field("content")
                .requireFieldMatch(false)
                .preTags("<em>")        //匹配到的字段前要加的内容
                .postTags("</em>");     //匹配到的字段后要加的内容

        //构建搜索条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightBuilder(highlightBuilder).build();

        //获取命中的数据
        SearchHits<DiscussPost> search = elasticTemplate.search(searchQuery, DiscussPost.class);
        //获取查询数据页
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, searchQuery.getPageable());
        //判空
        if(search.getTotalHits() <= 0){
            return null;
        }

        //将带有高亮内容的DiscussPost封装到page对象中
        //其实可以不封装到page，在业务中我们直接使用list就可以实现功能
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            DiscussPost discussPost = discussPostSearchHit.getContent();
            //替换为高亮信息
            if(discussPostSearchHit.getHighlightFields().get("title") != null){
                //取匹配到的第一块信息替换即可，即get(0)
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("title").get(0));
            }
            if(discussPostSearchHit.getHighlightFields().get("content") != null){
                discussPost.setContent(discussPostSearchHit.getHighlightFields().get("content").get(0));
            }
            //将帖子信息添加到list中
            list.add(discussPost);
        }

        //返回分页数据
        return new PageImpl<>(list, searchQuery.getPageable(), search.getTotalHits());
    }

}
