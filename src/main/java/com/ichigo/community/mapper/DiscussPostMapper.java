package com.ichigo.community.mapper;

import com.ichigo.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPost(int userId, int offset, int limit);

    //@Param注解给字段起别名
    //<if>
    int selectDiscussPostRows(@Param("userId") int userId);

}
