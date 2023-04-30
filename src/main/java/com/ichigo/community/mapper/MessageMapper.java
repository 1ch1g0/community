package com.ichigo.community.mapper;

import com.ichigo.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，每个会话显示最新的一条消息
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前会话数量
    int selectConversationCount(int userId);

    //查询单个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询单个会话的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数（分所有会话未读数和单个会话未读数，使用第二个参数区分控制）
    int selectLetterUnreadCount(int userId, String conversationId);

}
