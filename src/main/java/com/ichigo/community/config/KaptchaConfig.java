package com.ichigo.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    /**
     * 实例化kaptcha核心接口Producer
     * @return
     */
    @Bean
    public Producer kaptchaProducer(){
        Properties properties = new Properties();
        //长宽
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        //字体
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        //字符范围及长度
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        //干扰
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        //实例化核心接口
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        //使用Config给kaptcha传入配置参数，Config需要依赖于Properties对象(key-value类型)
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
