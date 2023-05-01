package com.ichigo.community;

import org.aspectj.weaver.ast.Or;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        redisTemplate.opsForValue().set("test:value", 1);
        redisTemplate.opsForValue().increment("test:value");
        redisTemplate.opsForValue().increment("test:value");
        System.out.println(redisTemplate.opsForValue().get("test:value"));
    }

    @Test
    public void testHash(){
        String key = "test:hash";
        BoundHashOperations operations = redisTemplate.boundHashOps(key);
        operations.put("username", "zhangsan");
        operations.put("age", 18);
        System.out.println(operations.get("username") + ", " + operations.get("age"));
    }

}
