package cn.nu11cat.mpdemo.service.impl;

import cn.nu11cat.mpdemo.entity.User;
import cn.nu11cat.mpdemo.mapper.UserMapper;
import cn.nu11cat.mpdemo.req.UserDeleteReq;
import cn.nu11cat.mpdemo.req.UserUpdateReq;
import cn.nu11cat.mpdemo.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 根据邮箱更新用户信息，只更新非空字段
     */
    public void updateByEmail(UserUpdateReq req) {
        if (!StringUtils.hasText(req.getEmail())) {
            throw new RuntimeException("邮箱不能为空"); // 可换成自定义异常
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getEmail, req.getEmail());

        User user = new User();
        if (StringUtils.hasText(req.getName())) user.setName(req.getName());
        if (req.getAge() != null) user.setAge(req.getAge());

        boolean updated = update(user, wrapper);
        if (!updated) {
            throw new RuntimeException("更新失败或邮箱不存在");
        }
    }

    public void deleteByEmail(UserDeleteReq req) {
        if (!StringUtils.hasText(req.getEmail())) {
            throw new RuntimeException("邮箱不能为空"); // 可换成自定义异常
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getEmail, req.getEmail());

        boolean deleted = remove(wrapper);
        if (!deleted) {
            throw new RuntimeException("更新失败或邮箱不存在");
        }
    }
}
