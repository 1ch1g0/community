package com.ichigo.community.service;

import com.ichigo.community.entity.LoginTicket;
import com.ichigo.community.entity.User;
import com.ichigo.community.mapper.LoginTicketMapper;
import com.ichigo.community.mapper.UserMapper;
import com.ichigo.community.util.CommunityConstant;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    /**
     * 根据用户id查询用户信息
     * @param id
     * @return
     */
    public User findById(int id){
        return userMapper.getById(id);
    }

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    public User findByName(String username){
        return userMapper.getByUsername(username);
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.getByUsername(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        //验证邮箱
        u = userMapper.getByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 激活用户
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code){
        User user = userMapper.getById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.getByUsername(username);
        if(user == null){
            map.put("usernameMsg", "账号不存在！");
            return map;
        }

        //验证状态
        if(user.getStatus() == 0){
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码不正确");
            return map;
        }

        //验证成功，生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 用户退出
     * @param ticket
     */
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

    /**
     * 查询ticket凭证
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return loginTicket;
    }

    /**
     * 修改用户头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId, headerUrl);
    }

    /**
     * 修改密码
     * @param user
     * @param originalPassword
     * @param newPassword
     * @return
     */
    public Map<String, Object> updatePassword(User user, String originalPassword, String newPassword){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(originalPassword)){
            map.put("originalPasswordMsg", "原密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(originalPassword)){
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }

        //校验密码
        originalPassword = CommunityUtil.md5(originalPassword + user.getSalt());
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if(!user.getPassword().equals(originalPassword)){
            map.put("originalPasswordMsg", "原密码错误！");
            return map;
        }
        if(user.getPassword().equals(newPassword)){
            map.put("newPasswordMsg", "新密码不能与原密码相同！");
            return map;
        }

        //修改密码
        userMapper.updatePassword(user.getId(), newPassword);

        return map;
    }

}
