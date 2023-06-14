package com.ichigo.community;

import com.ichigo.community.entity.DiscussPost;
import com.ichigo.community.entity.LoginTicket;
import com.ichigo.community.entity.Message;
import com.ichigo.community.entity.User;
import com.ichigo.community.mapper.DiscussPostMapper;
import com.ichigo.community.mapper.LoginTicketMapper;
import com.ichigo.community.mapper.MessageMapper;
import com.ichigo.community.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testGetUser(){
        User user = userMapper.getById(1);
        System.out.println(user);

        user = userMapper.getByUsername("SYSTEM");
        System.out.println(user);

        user = userMapper.getByEmail("ichigo@foxmail.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println("影响行数：" + rows + " id：" + user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(2, 1);
        System.out.println(rows);
        rows = userMapper.updateHeader(2, "http://www.nowcoder.com/10.png");
        System.out.println(rows);
        rows = userMapper.updatePassword(2, "xxhh123");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(101, 0, 10,0);
        for(DiscussPost discussPost : list){
            System.out.println(discussPost);
        }

        int count = discussPostMapper.selectDiscussPostRows(101);
        System.out.println(count);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectAndUpdateLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for(Message message : list){
            System.out.println(message);
        }

        int i = messageMapper.selectConversationCount(111);
        System.out.println("当前会话数为：" + i);

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 20);
        for(Message message : letters){
            System.out.println(message);
        }

        int i1 = messageMapper.selectLetterCount("111_112");
        System.out.println("单个会话私信数" + i1);

        int i2 = messageMapper.selectLetterUnreadCount(111, null);
        int i3 = messageMapper.selectLetterUnreadCount(111, "111_112");

        System.out.println("全部未读" + i2);
        System.out.println("单个未读" + i3);
    }
}
