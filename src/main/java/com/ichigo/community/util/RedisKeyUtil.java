package com.ichigo.community.util;

public class RedisKeyUtil {

    //定义key的分隔符
    private static final String SPLIT = ":";
    //定义实体所受赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    //定义用户所受赞的key的前缀
    private static final String PREFIX_USER_LIKE = "like:user";

    /**
     *  获取实体所受赞的key
     *  key表示对某个实体的赞
     *  格式：like:entity:entityType:entityId
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId){
        //拼接key
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     *  获取用户所受赞的key
     *  key表示对某个用户的赞
     *  格式：like:user:userId
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
}
