package com.ichigo.community.mapper.elasticsearch;

import com.ichigo.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRespository extends ElasticsearchRepository<DiscussPost, Integer> {
}
