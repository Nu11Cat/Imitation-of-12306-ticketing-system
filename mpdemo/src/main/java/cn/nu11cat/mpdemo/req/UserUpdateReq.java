package cn.nu11cat.mpdemo.req;

import lombok.Data;

@Data
public class UserUpdateReq {
    private String email; // 条件字段
    private String name;  // 可更新字段
    private Integer age;  // 可更新字段
    private String phone; // 可更新字段
}