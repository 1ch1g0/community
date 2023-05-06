package com.ichigo.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.ichigo.community.entity.Event;
import com.ichigo.community.entity.Message;
import com.ichigo.community.service.MessageService;
import com.ichigo.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    /**
     * 接收事件消息
     * @param record
     */
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        //判空
        if(record == null || record.value() == null){
            LOGGER.error("消息的内容为空！");
            return;
        }

        //将消息内容字符串转换为Event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        //判空
        if(event == null){
            LOGGER.error("消息格式错误！");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //设置拼接content所需的数据
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        //判断是否有额外的数据
        if(!event.getData().isEmpty()){
            //将额外的数据封装进content中
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        //发送消息
        messageService.addMessage(message);
    }

}
