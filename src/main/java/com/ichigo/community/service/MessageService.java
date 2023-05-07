package com.ichigo.community.service;

import com.ichigo.community.entity.Message;
import com.ichigo.community.mapper.MessageMapper;
import com.ichigo.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户的会话列表，每个会话显示最新的一条消息
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 查询当前会话数量
     * @param userId
     * @return
     */
    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询单个会话所包含的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findLetters(String conversationId, int offset, int limit){
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 查询单个会话的私信数量
     * @param conversationId
     * @return
     */
    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读私信数（分所有会话未读数和单个会话未读数，使用第二个参数区分控制）
     * @param userId
     * @param conversationId
     * @return
     */
    public int findLetterUnreadCount(int userId, String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 新增私信
     * @param message
     * @return
     */
    public int addMessage(Message message){
        //转换新增私信中的HTML标签
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //过滤新增私信中的敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));
        //添加私信
        return messageMapper.insertMessage(message);
    }

    /**
     * 将未读私信更新为已读
     * @param ids
     * @return
     */
    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * 获取某个主题最新的一条通知
     * @param userId
     * @param topic
     * @return
     */
    public Message findLatestNotice(int userId, String topic){
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 获取某个主题的通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 获取某个主题（/全部主题）的未读通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeUnreadCount(int userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    /**
     * 获取某个主题的通知列表
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findNotices(int userId, String topic, int offset, int limit){
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

}
