package cn.nu11cat.train.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`member`") // 对应数据库表名
public class Member {

    @TableId(type = IdType.ASSIGN_ID) // 使用雪花算法生成全局唯一ID
    private Long id;

    private String mobile; // 手机号，唯一约束由数据库控制

}