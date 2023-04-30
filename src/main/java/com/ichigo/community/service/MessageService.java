package com.ichigo.community.service;

import com.ichigo.community.entity.Message;
import com.ichigo.community.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

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

}
