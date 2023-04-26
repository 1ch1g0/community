package com.ichigo.community.controller.interceptor;

import com.ichigo.community.annotation.LoginRequired;
import com.ichigo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的对象是否为方法
        if(handler instanceof HandlerMethod){
            //是方法，强制转换为HandlerMethod对象
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //得到方法对象
            Method method = handlerMethod.getMethod();
            //读取方法上的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //如果注解不为空（即该方法配置了当前注解）并且ThreadLocal中获取的用户为空（即用户未登录）
            //重定向到登陆页面，不执行之后的请求
            if(loginRequired != null && hostHolder.getUser() == null){
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        //登陆状态没问题，放过拦截，执行后面的请求
        return true;
    }
}
