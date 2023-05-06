package com.ichigo.community.util;

public class RedisKeyUtil {

    //定义key的分隔符
    private static final String SPLIT = ":";
    //定义实体所受赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    //定义用户所受赞的key的前缀
    private static final String PREFIX_USER_LIKE = "like:user";
    //定义用户关注实体的key前缀
    private static final String PREFIX_FOLLOWEE = "followee";
    //定义用户被关注的key前缀
    private static final String PREFIX_FOLLOWER = "follower";
    //定义验证码的key前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //定义登陆凭证的key前缀
    private static final String PREFIX_TICKET = "ticket";
    //定义用户信息的key前缀
    private static final String PREFIX_USER = "user";

    /**
     *  获取实体所受赞的key
     *  key表示对某个实体的赞
     *  格式：like:entity:entityType:entityId  ->set(userId)
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
     *  格式：like:user:userId  ->int
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     *  获取用户关注的实体的key
     *  key表示用户id以及被该用户关注实体的类型，value表示被关注实体的id并使用关注时间作分数排序
     *  格式：followee:userId:entityType  ->zset(entityId, now)
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     *  获取实体被关注的用户的key
     *  key表示实体的类型和id，value表示关注的用户id并使用关注时间作分数排序
     *  格式：follower:entityType:entityId  ->zset(userId, now)
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     *  获取登录验证码的key
     *  key表示验证码归属者，value表示验证码
     *  格式：kaptcha:owner  ->String
     * @param owner
     * @return
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     *  获取登录凭证的key
     *  key表示登陆凭证字符串和前缀拼接的串，value表示登陆凭证对象
     *  格式：ticket:ticket  ->String
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     *  获取用户信息的key
     *  key表示用户信息key，value表示用户信息
     *  格式：user:userId  ->String
     * @param userId
     * @return
     */
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

}
