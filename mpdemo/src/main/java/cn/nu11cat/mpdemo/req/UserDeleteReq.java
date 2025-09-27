package cn.nu11cat.mpdemo.req;

import lombok.Data;

@Data
public class UserDeleteReq {
    private String email; // 条件字段
}