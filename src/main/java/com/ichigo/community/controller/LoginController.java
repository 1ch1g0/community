package com.ichigo.community.controller;

import com.google.code.kaptcha.Producer;
import com.ichigo.community.entity.User;
import com.ichigo.community.service.UserService;
import com.ichigo.community.util.CommunityConstant;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("server.servlet.context-path")
    private String contextPath;

    /**
     * 响应注册页面请求
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    /**
     * 响应登录页面请求
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 响应注册请求
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            //注册成功
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else{
            //注册失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 响应激活请求
     * @param model
     * @param userId
     * @param code
     * @return
     */
    //http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已激活！");
            model.addAttribute("target", "/index");
        }else{
            model.addAttribute("msg", "激活失败，您提供的激活码有误！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 响应生成图片验证码
     * @param response
     * @param session
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码(先生成随机验证码，再用随机验证码生成图片)
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session
        //session.setAttribute("kaptcha", text);

        //生成验证码的归属唯一标识(随机字符串)
        String kaptchaOwner = CommunityUtil.generateUUID();
        //将唯一标识存入cookie
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            //创建字节流
            OutputStream os = response.getOutputStream();
            //将图片输出到字节流中返回给浏览器
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            LOGGER.error("响应验证码失败：" + e.getMessage());
        }
    }

    /**
     * 响应登录请求
     * @param username
     * @param password
     * @param code
     * @param rememberMe
     * @param model
     * @param session
     * @param response
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, HttpSession session, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        //检查账号和密码
        //获取凭证超时时间
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            //返回的map中有ticket凭证说明验证成功，即登陆成功，将凭证储存在cookie中
            //将凭证储存在cookie中
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            //设置cookie的有效路径（要求登录凭证在所有页面都有效）
            cookie.setPath(contextPath);
            //设置cookie的过期时间
            cookie.setMaxAge(expiredSeconds);
            //返回cookie给前端
            response.addCookie(cookie);
            //重定向到首页
            return "redirect:/index";
        }else {
            //验证失败，返回错误信息
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            //返回登陆页面
            return "/site/login";
        }
    }

    /**
     * 响应退出登录请求
     * @param ticket
     * @return
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
