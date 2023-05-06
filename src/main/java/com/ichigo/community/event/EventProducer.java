package com.ichigo.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.ichigo.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     * @param event
     */
    public void fireEvent(Event event){
        //将事件发布到指定的主题，将消息内容转换为字符串传输
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
