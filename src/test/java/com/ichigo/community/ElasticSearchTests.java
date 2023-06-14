package com.ichigo.community;

import com.ichigo.community.entity.DiscussPost;
import com.ichigo.community.mapper.DiscussPostMapper;
import com.ichigo.community.mapper.elasticsearch.DiscussPostRespository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRespository discussRespository;

    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;

    @Test
    public void testInsert(){
        discussRespository.save(discussMapper.selectDiscussPostById(241));
        discussRespository.save(discussMapper.selectDiscussPostById(242));
        discussRespository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussRespository.saveAll(discussMapper.selectDiscussPosts(101, 0, 100,0));
        discussRespository.saveAll(discussMapper.selectDiscussPosts(102, 0, 100,0));
        discussRespository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100,0));
        discussRespository.saveAll(discussMapper.selectDiscussPosts(111, 0, 100,0));
        discussRespository.saveAll(discussMapper.selectDiscussPosts(112, 0, 100,0));
        discussRespository.saveAll(discussMapper.selectDiscussPosts(131, 0, 100,0));
        discussRespository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100,0));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水。");
        discussRespository.save(post);
    }

    @Test
    public void testDelete(){
        discussRespository.deleteById(231);
    }

    @Test
    public void testSearchByTemplate(){
        /*NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        Page<DiscussPost> page = elasticTemplate.*/

        //高亮条件
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")     //给哪些自动设置高亮
                .field("content")
                .requireFieldMatch(false)
                .preTags("<em>")        //匹配到的字段前要加的内容
                .postTags("</em>");     //匹配到的字段后要加的内容

        //构建搜索条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightBuilder(highlightBuilder).build();

        SearchHits<DiscussPost> search = elasticTemplate.search(searchQuery, DiscussPost.class);
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, searchQuery.getPageable());
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            //高亮的内容
            System.out.println(discussPostSearchHit.getHighlightFields());
            //原始的内容
            System.out.println(discussPostSearchHit.getContent());
        }

        //将带有高亮内容的DiscussPost封装到page对象中
        //其实可以不封装到page，在业务中我们直接使用list就可以实现功能
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            DiscussPost discussPost = discussPostSearchHit.getContent();
            if(discussPostSearchHit.getHighlightFields().get("title") != null){
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("title").get(0));
            }
            if(discussPostSearchHit.getHighlightFields().get("content") != null){
                discussPost.setTitle(discussPostSearchHit.getHighlightFields().get("content").get(0));
            }
            list.add(discussPost);
        }

        Page<DiscussPost> pageInfo = new PageImpl<>(list, searchQuery.getPageable(), search.getTotalHits());
        System.out.println(pageInfo.getTotalElements());
        System.out.println(pageInfo.getTotalPages());
        System.out.println(pageInfo.getNumber());
        System.out.println(pageInfo.getSize());
        for (DiscussPost discussPost : pageInfo) {
            System.out.println(discussPost);
        }
    }

}
