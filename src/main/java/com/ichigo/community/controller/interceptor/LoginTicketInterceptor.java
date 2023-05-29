package com.ichigo.community.controller.interceptor;

import com.ichigo.community.entity.LoginTicket;
import com.ichigo.community.entity.User;
import com.ichigo.community.service.UserService;
import com.ichigo.community.util.CookieUtil;
import com.ichigo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //在Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if(ticket != null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //凭证有效
                //根据凭证查询用户信息
                User user = userService.findById(loginTicket.getUserId());
                //在本次请求中持有用户（使用ThreadLocal）
                hostHolder.setUser(user);
                //构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    //在Controller之后，模板引擎之前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //获取用户信息
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            //将用户信息注入模板，以便使用
            modelAndView.addObject("loginUser", user);
        }
    }

    //在Controller之后，模板引擎之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //请求完成，清理ThreadLocal中存储的用户信息
        hostHolder.clear();
        //清理SecurityContextHolder
        //SecurityContextHolder.clearContext();
    }
}
