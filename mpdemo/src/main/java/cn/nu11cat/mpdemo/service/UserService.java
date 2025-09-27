package cn.nu11cat.mpdemo.service;

import cn.nu11cat.mpdemo.entity.User;
import cn.nu11cat.mpdemo.mapper.UserMapper;
import cn.nu11cat.mpdemo.req.UserDeleteReq;
import cn.nu11cat.mpdemo.req.UserUpdateReq;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

public interface UserService extends IService<User> {
    public void updateByEmail(UserUpdateReq req);

    void deleteByEmail(UserDeleteReq req);
}
