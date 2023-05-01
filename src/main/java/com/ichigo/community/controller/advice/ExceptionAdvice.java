package com.ichigo.community.controller.advice;

import com.ichigo.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.error("服务器发生异常：" + e.getMessage());
        //获取详细错误信息
        for(StackTraceElement element : e.getStackTrace()){
            //每个element记录一条有序的错误信息
            LOGGER.error(element.toString());
        }

        //浏览器的请求可能是普通请求希望返回页面，也可能是异步请求希望返回JSON，所以不能只是重定向
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            //表示是异步请求，返回一个字符串，通过浏览器转为JSON格式
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        }else{
            //表示是普通请求，重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
