package com.ichigo.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    /**
     * 从request中获取指定的cookie信息
     * @param request
     * @param name
     * @return
     */
    public static String getValue(HttpServletRequest request, String name){
        if(request == null || name == null){
            throw new IllegalArgumentException("参数为空！");
        }

        //遍历cookie数组从中寻找所求cookie
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        //没有要获取的cookie
        return null;
    }
}
