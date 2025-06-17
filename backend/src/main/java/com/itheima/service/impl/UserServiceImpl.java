package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import com.itheima.service.FolderService;
import com.itheima.service.UserService;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final FolderService folderService;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User findByUserName(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public void register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.getMD5String(password));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        
        // 用户注册成功后，自动创建用户文件目录
        String folderName = "user_" + user.getId();
        folderService.createFolder(user.getId(), 0L, folderName);
    }

    @Override
    @Transactional
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long id = ((Integer) map.get("id")).longValue(); // 转换为Long类型

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getUserPic, avatarUrl);
        userMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void updatePwd(String newPwd) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long id = ((Integer) map.get("id")).longValue(); // 转换为Long类型
        log.info("开始更新用户密码，用户ID: {}", id);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                .set(User::getPassword, Md5Util.getMD5String(newPwd));
        
        log.info("构建更新条件: {}", updateWrapper.getSqlSet());
        int rows = userMapper.update(null, updateWrapper);
        log.info("密码更新完成，影响行数: {}", rows);
    }
}