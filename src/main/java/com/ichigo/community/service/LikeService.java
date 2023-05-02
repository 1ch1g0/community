package com.ichigo.community.service;

import com.ichigo.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
    public void like(int userId, int entityType, int entityId, int entityUserId){
        //因为在点赞的过程中有两次更新操作，使用redis编程事务来保证事务性
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //获取实体的赞的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                //获取用户的赞的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //判断key是否存在(放在事务外，因为事务内的查询是无效的)
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();
                if(isMember){
                    //存在，即已点赞，删除点赞信息
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //将被点赞者的赞-1
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    //不存在，未点赞，添加点赞信息
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    //将被点赞者的赞+1
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
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

    /**
     * 获取某个用户获得的赞
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId){
        //获取用户被赞key
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        //使用被赞key获取用户被赞数
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        //判空返回
        return count == null ? 0 : count;
    }
}
