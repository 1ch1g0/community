package com.ichigo.community.service;

import com.ichigo.community.entity.User;
import com.ichigo.community.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findById(int id){
        return userMapper.getById(id);
    }
}
