Imitation-of-12306-ticketing-system

#### 仿12306的分布式高并发票务系统

#### 简介

- 技术栈：Java17，SpringBoot3，SpringCloud，SpringCloudAlibaba，MySQL，Redis，MyBatis-Plus，RocketMQ等
- Redis分布式细颗粒度锁串行操作，Redis+Lua 库存预扣减，数据库层乐观锁，异常回退和Seata事务，**保证不超卖/数据一致性**
- 令牌桶/前端图片验证码限流，Sentinel熔断降级，MQ 排队削峰异步，Redis防护缓存穿透、击穿、雪崩，**应对秒杀**
- 实现幂等发送和消费，添加消息发送确认机制，消息持久化，自动ACK，消费端重试机制，死信队列，**保证消息不丢失**
- 使用 Redis 缓存余票信息，响应速度提升约**5.8**倍，单机下接口吞吐量提升约**75.68%**，并使用缓存空值 + 布隆过滤器防穿透，使用互斥锁防击穿，过期时间随机化防雪崩
- 使用 Redis + Lua 库存预扣减配合 RocketMQ 异步，在保证不超卖和数据一致的前提下，响应速度提升约**22.3**倍，单机下接口吞吐量提升约**17.09%**

#### 文件结构

```
Imitation-of-12306-ticketing-system/
│
├─ admin/             # 管理端
├─ web/               # 用户端
├─ train/             # V1：完成业务功能，基本实现不超卖和一致性
├─ train-mp/          # V2：MP重构后端代码，更加简洁易读
├─ train-opt/         # V3：最佳实践，性能优化，保证高并发下不超买和一致性
├─ sql/               # SQL脚本
├─ doc/               # 文档
├─ resource/          # 图片、脚本、其他配置文件等
├─ .gitignore         # Git忽略文件
├─ LICENSE            # 许可证
└─ README.md          # 项目说明
```

#### 文档目录

[技术栈](./doc/ticket_1.md)	[系统架构](./doc/ticket_2.md)	[核心功能/模块](./doc/ticket_3.md)	[数据库表](./doc/ticket_4.md)	[各版本说明](./doc/ticket_5.md)

[功能实现](./doc/ticket_6.md)	[组件/框架/中间件集成与配置](./doc/ticket_7.md)	[高并发和性能优化](./doc/ticket_8.md)	[分布式和微服务相关](./doc/ticket_9.md)

[压测](./doc/ticket_10.md)	[问题排查/解决](./doc/ticket_11.md)	[总结](./doc/ticket_12.md)	[其他](./doc/ticket_13.md)

#### 其他

v3追求最佳实践，但是仍然存在不足和优化点，如果你想进一步优化，这里为你提供更多思路：

1，添加购票请求的“车次”是否存在，“日期”是否合规，用户是否已经购买“该天该车次的车票”---单纯的在购票前CRUD作边界校验

2，添加事务消息---虽然消息发送确认机制，消息持久化，自动ACK，消费端重试机制，死信队列等已经保证高并发下消息不会丢失，但是添加事务消息更好--- `syncSend()`改成`sendMessageInTransaction`事务消息发送，添加本地事务执行器，添加MQ回查方法

3，添加监控补偿机制或定时任务补偿机制，进一步保障消息不丢失和数据的一致性......

4，分库分表......

5，Redis 集群 + 持久化......