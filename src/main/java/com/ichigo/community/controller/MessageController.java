package com.ichigo.community.controller;

import com.ichigo.community.entity.Message;
import com.ichigo.community.entity.Page;
import com.ichigo.community.entity.User;
import com.ichigo.community.service.MessageService;
import com.ichigo.community.service.UserService;
import com.ichigo.community.util.CommunityUtil;
import com.ichigo.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 响应获取会话列表请求
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //获取会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        //将额外的信息和会话列表封装在一起（包括每个会话的未读数和私信数）
        List<Map<String, Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message : conversationList){
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                //为了显示对方的头像，我们需要判断会话对方是谁
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findById(targetId));
                //将封装数据的map添加到新的会话列表中
                conversations.add(map);
            }
        }
        //将新会话列表添加到thymeleaf模板中
        model.addAttribute("conversations", conversations);

        //查询总的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    /**
     * 响应查看私信详情请求
     * @param conversationId
     * @param page
     * @return
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //获取私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        //将额外的信息（消息的发送者）和私信列表封装在一起
        List<Map<String, Object>> letters = new ArrayList<>();
        //封装数据
        if(letterList != null){
            for(Message message : letterList){
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findById(message.getFromId()));
                //将封装数据添加到私信列表中
                letters.add(map);
            }
        }
        //将私信列表添加到thymeleaf模板中
        model.addAttribute("letters", letters);
        //将会话对方的信息添加到模板中
        model.addAttribute("target", getLetterTarget(conversationId));

        //将未读消息转换为已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    /**
     * 获取会话对方的信息
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId){
        //分割字符串
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() == id0){
            return userService.findById(id1);
        }else{
            return userService.findById(id0);
        }
    }

    /**
     * 响应发送私信请求(异步请求)
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        //根据用户名获取目标用户的信息
        User target = userService.findByName(toName);
        //判空
        if(target == null){
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        if(StringUtils.isBlank(content)){
            return CommunityUtil.getJSONString(2, "内容不能为空！");
        }

        //构造私信内容
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else{
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());

        //发送私信
        messageService.addMessage(message);

        //返回成功信息
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 获取会话中未读私信的id
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();

        if(letterList != null){
            for (Message message : letterList) {
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

}
