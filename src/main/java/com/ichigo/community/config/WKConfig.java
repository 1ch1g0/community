package com.ichigo.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WKConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WKConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init(){
        //创建WK图片目录
        File file = new File(wkImageStorage);
        if(!file.exists()){
            file.mkdir();
            LOGGER.info("创建WK图片目录：" + wkImageStorage);
        }
    }

}
