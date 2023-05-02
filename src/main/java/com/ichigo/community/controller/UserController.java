package com.ichigo.community.controller;

import com.ichigo.community.annotation.LoginRequired;
import com.ichigo.community.entity.User;
import com.ichigo.community.service.LikeService;
import com.ichigo.community.service.UserService;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 响应账户设置页面请求
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传用户头像
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        //获取原文件文件名
        String fileName = headerImage.getOriginalFilename();
        //截取文件后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        //更新当前用户头像的路径（web访问路径）
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * 下载获取用户头像
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //响应图片
        response.setContentType("image/" + suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            LOGGER.error("读取头像失败：" + e.getMessage());
        }
    }


    /**
     * 修改密码
     * @param model
     * @param originalPassword
     * @param newPassword
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String originalPassword, String newPassword){
        Map<String, Object> map = userService.updatePassword(hostHolder.getUser(), originalPassword, newPassword);
        if(map.size() == 0){
            //修改成功
            return "redirect:/index";
        }else{
            //修改失败
            model.addAttribute("originalPasswordMsg", map.get("originalPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    /**
     * 响应个人主页请求
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        //查询用户信息
        User user = userService.findById(userId);
        //判空
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        //将用户信息添加到thymeleaf模板中
        model.addAttribute("user", user);
        //获取被赞数
        int likeCount = likeService.findUserLikeCount(userId);
        //将被赞数添加到模板中
        model.addAttribute("likeCount", likeCount);

        return "/site/profile";
    }
}
