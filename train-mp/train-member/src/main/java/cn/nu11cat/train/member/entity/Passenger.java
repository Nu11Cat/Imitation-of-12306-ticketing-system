package cn.nu11cat.train.member.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Passenger {
    private Long id;

    private Long memberId;

    private String name;

    private String idCard;

    private String type;

    private Date createTime;

    private Date updateTime;

}