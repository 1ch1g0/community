package com.ichigo.community.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //MD5加密
    public static String md5(String key){
        //判空
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 获取JSON字符串
     * @param code 状态编码
     * @param msg 提示信息
     * @param map 业务数据
     * @return JSON格式的字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    //重载方法
    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    //重载方法
    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }
}
