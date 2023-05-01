package com.ichigo.community.service;

import com.ichigo.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * 使用Set类型存储点赞数据，方便获取点赞用户
     * key为点赞实体key，value为点赞用户
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId, int entityType, int entityId){
        //获取点赞实体key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //判断key是否存在
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(isMember){
            //存在，即已点赞，删除点赞信息
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        }else{
            //不存在，未点赞，添加点赞信息
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    /**
     * 获取某实体点赞的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 获取用户对某实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

}
