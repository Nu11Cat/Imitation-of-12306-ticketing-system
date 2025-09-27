package cn.nu11cat.mpdemo.controller;

import cn.nu11cat.mpdemo.entity.User;
import cn.nu11cat.mpdemo.req.UserDeleteReq;
import cn.nu11cat.mpdemo.req.UserUpdateReq;
import cn.nu11cat.mpdemo.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    // 查询所有
    @GetMapping("/all")
    public List<User> getAll() {
        return userService.list();
    }

    // 根据 id 查询
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    // 添加
    @PostMapping("/add")
    public String add(@RequestBody User user) {
        // 1. 校验 email 是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, user.getEmail());
        boolean exists = userService.count(wrapper) > 0;

        if (exists) {
            return "该邮箱已存在"; // 或者抛出自定义异常
        }

        // 2. 保存用户
        boolean saved = userService.save(user);
        return saved ? "添加成功" : "添加失败";
    }


    // 更新
    @PutMapping("/update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    // 根据条件更新
    @PutMapping("/updateByEmail")
    public String updateByEmail(@RequestBody UserUpdateReq req) {

        try {
            userService.updateByEmail(req);
            return "更新成功";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }


    // 删除
    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Long id) {
        return userService.removeById(id);
    }

    //根据邮箱删除
    @DeleteMapping("/deleteByEmail")
    public String deleteByEmail(@RequestBody UserDeleteReq req) {

        try {
            userService.deleteByEmail(req);
            return "删除成功";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    // 条件查询示例：可按名字和年龄筛选
    @GetMapping("/search1")
    public List<User> search(@RequestParam(required = false) String name,
                             @RequestParam(required = false) Integer age) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(User::getName, name);
        }
        if (age != null) {
            wrapper.eq(User::getAge, age);
        }

        return userService.list(wrapper);
    }

    // 分页 + 条件查询
    @GetMapping("/search2")
    public Page<User> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer age,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(User::getName, name);
        }
        if (age != null) {
            wrapper.eq(User::getAge, age);
        }

        Page<User> page = new Page<>(pageNum, pageSize);
        return userService.page(page, wrapper);
    }

}
