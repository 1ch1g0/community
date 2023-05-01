package com.ichigo.community.util;

public class RedisKeyUtil {

    //定义key的分隔符
    private static final String SPLIT = ":";
    //定义key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    /**
     *  获取点赞实体key
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

}
