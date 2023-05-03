package com.ichigo.community.service;

import com.ichigo.community.entity.User;
import com.ichigo.community.util.CommunityConstant;
import com.ichigo.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId){
        //redis事务管理关注功能
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //获取关注key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //获取粉丝key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    /**
     * 取关
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollow(int userId, int entityType, int entityId){
        //redis事务管理关注功能
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //获取关注key
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                //获取粉丝key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    /**
     * 获取关注数
     * @param userId
     * @param entityType
     * @return
     */
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 获取粉丝数
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已关注该实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 查询用户关注的人
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        //获取redis中用户关注的人的key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //使用key和分页信息获取关注列表信息（被关注用户id），redis返回的Set是有序的
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        //判空
        if(targetIds == null){
            return null;
        }

        //用户关注列表
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            //用来封装多种数据的map
            Map<String, Object> map = new HashMap<>();
            //封装被关注用户信息
            User user = userService.findById(targetId);
            map.put("user", user);
            //封装被关注用户被关注的时间信息（分数转毫秒时间）
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            //将map添加到列表中
            list.add(map);
        }
        //返回关注列表信息
        return list;
    }

    /**
     * 查询用户粉丝
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        //与findFollowees方法同理，获取key
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        //获取粉丝id集合
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        //判空
        if(targetIds == null){
            return null;
        }

        //用户粉丝列表
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            //用来封装多种数据的map
            Map<String, Object> map = new HashMap<>();
            //封装用户粉丝信息
            User user = userService.findById(targetId);
            map.put("user", user);
            //封装用户粉丝关注时间（分数转毫秒时间）
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            //将map添加到列表中
            list.add(map);
        }
        //返回关注列表信息
        return list;
    }

}
