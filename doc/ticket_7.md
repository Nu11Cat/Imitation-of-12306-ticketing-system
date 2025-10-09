### 组件/框架/中间件集成与配置

### 雪花算法生成分布式 ID

系统中需要全局唯一 ID（如订单号、令牌 ID、日志 ID 等），分布式微服务下多实例生成时不能出现重复。

**一般的生成分布式 ID方式有**：

1，UUID：简单生成，全球唯一，但是长度大，索引效率低，适合对全局唯一性要求高但是性能要求不高的场景。

2，Redis 自增：高性能，全局唯一，但是依赖Redis，适合高并发系统。

3，雪花算法：高性能、趋势递增、无中心依赖，但是需要保证机器 ID 唯一，适合高并发分布式微服务系统。

**为什么选择雪花算法**：

1，ID全局唯一（由时间戳、机器 ID、序列号组成，64 位长整型）。

2，ID 按时间递增，有利于数据库索引效率。

3，生成速度快，几乎无锁，适合高并发场景。

4，无中心依赖，不依赖数据库或协调中心。

**具体实现**：Hutool 的 SnowUtil。各微服务实例分配不同的机器 ID，生成的 ID 就能保证全局唯一。

**为什么不使用Redis 自增**：

1，Redis自增每次生成ID需要访问 Redis 并执行 `INCR` ，增加网路开销，成为高并发性能瓶颈。而雪花算法在本地生成，无锁，无网络调用，快。

2，Redis自增依赖 Redis 单点（或者主从/集群），受Redis宕机或者网络问题影响。雪花算法在本地生成，不依赖外部存储，稳定。

3，在分布式系统扩展场景下，Redis自增需要统一访问同一个 Redis 实例或用集群管理 ID，扩展复杂。雪花算法通过分配不同机器 ID 或工作节点 ID确保ID唯一，扩展简单。

4，所以雪花算法更适合对于高并发、低延迟、可扩展、全局唯一要求高的票务系统。

### Nacos 

**Nacos** 是一个动态服务发现、配置管理和服务管理平台。

在微服务架构中，各服务实例数量多、配置分散且可能动态变化，需要集中管理配置和服务注册发现。

**核心作用**：

**配置**：

```properties
# 注册中心应用名
spring.application.name=business

# 启动环境，nacos会根据环境读不同的配置dataId：business-dev.properties
spring.profiles.active=dev

# nacos server配置中心地址
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
# 配置中心文件后缀，默认properties
spring.cloud.nacos.config.file-extension=properties
# 配置中心命名空间
#spring.cloud.nacos.config.namespace=train
spring.cloud.nacos.config.namespace=e0ac609b-ff99-4b1f-8f85-caaec475d91f
# nacos server注册中心地址
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
#服务发现命名空间
#spring.cloud.nacos.discovery.namespace=train
spring.cloud.nacos.discovery.namespace=e0ac609b-ff99-4b1f-8f85-caaec475d91f
```

### Seata 

**Seata** 是一款开源的分布式事务解决方案。

在微服务系统中，一次业务操作可能涉及多服务、多数据库表更新，需要保证跨服务的数据一致性。

**配置**：

Seata 的注册中心配置和 Seata Server 的 `application.yml` 必须保持一致。

`seata.tx-service-group` 必须在 Nacos 配置文件中配置映射

```properties
# seata注册中心，要和seata server的application.yml配置保持一致
##注册中心类型
seata.registry.type=nacos
##Seata Server 名称
seata.registry.nacos.application=seata-server
##Nacos 地址
seata.registry.nacos.server-addr=127.0.0.1:8848
##Nacos 分组
seata.registry.nacos.group=SEATA_GROUP
##命名空间
seata.registry.nacos.namespace=e0ac609b-ff99-4b1f-8f85-caaec475d91f
## 默认的用户名密码
seata.registry.nacos.username=nacos
seata.registry.nacos.password=nacos

# seata配置中心，要和seata server的application.yml配置保持一致
##配置中心类型
seata.config.type=nacos
##
seata.config.nacos.server-addr=127.0.0.1:8848
##
seata.config.nacos.group=SEATA_GROUP
##
seata.config.nacos.namespace=e0ac609b-ff99-4b1f-8f85-caaec475d91f
##Seata 配置文件 ID
seata.config.nacos.dataId=seataServer.properties
##
seata.config.nacos.username=nacos
seata.config.nacos.password=nacos

#事务分组名称
seata.tx-service-group=train-group
```



### Sentinel 

高并发访问余票查询、购票等核心接口，需要动态限流、降级，保护系统稳定性。



**配置**：

Sentinel 默认从本地加载规则，这里通过 `Nacos` 持久化规则，方便统一管理

```properties
# sentinel控台
##应用和 Sentinel 控制台的通信端口
spring.cloud.sentinel.transport.port=8719
##Sentinel 控制台地址
spring.cloud.sentinel.transport.dashboard=localhost:8080

# sentinel注册配置于nacos
##流控规则存储在 Nacos 地址
spring.cloud.sentinel.datasource.flow.nacos.serverAddr=127.0.0.1:8848
##
spring.cloud.sentinel.datasource.flow.nacos.namespace=e0ac609b-ff99-4b1f-8f85-caaec475d91f
##Nacos 中存储流控规则的 逻辑分组
spring.cloud.sentinel.datasource.flow.nacos.groupId=DEFAULT_GROUP
##Nacos 中存储流控规则的 DataId
spring.cloud.sentinel.datasource.flow.nacos.dataId=sentinel-business-flow
##规则类型
spring.cloud.sentinel.datasource.flow.nacos.ruleType=flow
```

### RocketMQ

在高并发购票场景下，需要异步排队处理请求，削峰填谷，保证系统稳定。

**连接配置**：

```properties
#RocketMQ NameServer 地址（多个用分号分隔），生产环境推荐集群部署
rocketmq.name-server=http://localhost:9876
#生产者分组名称，用于标识一类生产者，同一业务逻辑的生产者建议用固定 group。
rocketmq.producer.group=default
```

### Redis

高并发余票查询、验证码校验、秒杀令牌等场景需要快速响应，减少数据库压力。

**配置**：

```properties
#Redis数据库
##连接地址
spring.data.redis.host=地址
##连接端口
spring.data.redis.port=6379
##访问密码
spring.data.redis.password=Redis000

#Spring Cache
##Spring Cache 缓存类型
spring.cache.type=redis
##是否使用统一前缀，避免 key 冲突
spring.cache.redis.use-key-prefix=true
##缓存 key 前缀
spring.cache.redis.key-prefix=train_cache_
##是否缓存空值，避免缓存穿透
spring.cache.redis.cache-null-values=true
##默认缓存过期时间
spring.cache.redis.time-to-live=60s
```

### MySQL

**为什么选择MySQL**：

1，成熟稳定：关系型数据库里应用最广，功能完善，社区活跃，生产可控。

2，票务场景适配：车票系统强依赖事务（购票涉及库存扣减、订单生成），MySQL天然支持 ACID 事务，能保证数据一致性。

3，性价比高：开源、易运维、生态丰富（和 MyBatis、Seata、ShardingSphere、各种中间件无缝集成）。

**配置**：

```properties
# 数据库连接
## 数据库地址，包含字符编码、时区和自动重连选项
spring.datasource.url=jdbc:mysql://localhost:3306/train-business?characterEncoding=UTF8&autoReconnect=true&serverTimezone=Asia/Shanghai
## 数据库用户名密码
spring.datasource.username=train
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

```properties
# mybatis xml路径，保证 MyBatis 能扫描到对应 SQL 映射文件
mybatis.mapper-locations=classpath:/mapper/**/*.xml
# 对 Mapper 层开启 trace 级别日志，用于调试 SQL 执行情况
logging.level.cn.nu11cat.train.business.mapper=trace
```

### MyBatis持久层框架

MyBatis是一个基于 Java 的持久层框架，简单易用灵活、支持注解和 XML 映射，被广泛使用。

**核心作用**：简化 CRUD 开发，保证数据库操作可控。

```properties
# mybatis xml路径，保证 MyBatis 能扫描到对应 SQL 映射文件
mybatis.mapper-locations=classpath:/mapper/**/*.xml
# 对 Mapper 层开启 trace 级别日志，用于调试 SQL 执行情况
logging.level.cn.nu11cat.train.business.mapper=trace
```

### PageHelper分页插件

PageHelper 是 MyBatis 的一个开源分页插件。简单易用，与 MyBatis/Spring 高度兼容，功能丰富。

**核心作用**：解决分页逻辑，实现轻量级分页功能

### MyBatis-Plus

是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，简化开发、提高效率。

### Validation校验框架

Validation校验框架是一套用于自动检查数据是否合法的工具和规范。声明式，标准化规范，与Spring 等完美支持，支持自定义校验注解扩展性强。

**核心作用**：将校验逻辑（如非空、长度、格式、范围等）从业务代码中剥离出来，通过声明式注解（如 `@NotNull`, `@Email`）直接标注在模型的字段上，实现优雅、统一的参数校验。







### Quartz调度框架

Quartz 是一个开源的、功能丰富的作业调度库。

**核心作用**：定时任务管理，如每日生成两周后车次余票等信息。保证业务流程自动化。

**为什么选择Quartz**：

1，成熟稳定，是 Java 生态中广泛使用的调度框架。

2，分布式支持，在分布式环境中保证任务不会重复执行。

3，调度策略灵活：支持 CRON 表达式、简单间隔、日历调度等多种方式。

4，任务持久化：任务状态、执行记录可持久化到数据库，方便监控与回溯。

**为什么不使用Spring 自带的 @Scheduled注解**：

1，分布式支持有限，`@Scheduled` 仅在单节点 JVM 内部调度任务，无法保证在多实例/分布式环境中只执行一次任务。

2，持久化和容错能力不足，因为Quartz可持久化到数据库，而`@Scheduled` 任务调度信息在内存中维护，重启后任务状态丢失。

3，@Scheduled 不支持复杂调度策略，如按日历、指定时区或组合多条件触发。

4，对于简单的定时任务使用 Spring `@Scheduled`；而高并发、分布式票务系统需要更强大的Quartz。



### OpenFeign框架

OpenFeign 是一个基于 Java 的声明式 HTTP 客户端框架，常用于简化服务间的 REST API 调用。

**核心作用**：微服务之间调用 HTTP API。支持负载均衡、容错、超时控制，与 Spring Cloud 微服务体系无缝集成。

**为什么选择OpenFeign**：

1，声明式调用：通过接口 + 注解方式定义远程调用，代码清晰、易维护。

2，负载均衡：与 Ribbon/LB 集成，自动分配请求到可用服务实例。

3，与 Spring Cloud 生态集成良好：与 Eureka/Nacos、Ribbon、Hystrix/Sentinel 配合使用，支持服务发现、限流和熔断。

4，可扩展：可自定义拦截器、日志、解码器、编码器等。

**其他方案**：

1，对大多数业务场景，尤其是微服务架构中，OpenFeign 是最佳实践。

2，如果有极端性能要求或非常复杂的调用链，可以考虑自定义 HTTP 客户端方案。



### Kaptcha图形验证码库	

Kaptcha 是一个基于 Java 的图形验证码生成库，常用于防止自动化脚本攻击，如刷票、爬虫恶意注册或暴力破解密码。虽然近年来出现了更现代、用户体验更友好的验证方案（如行为验证、短信/邮箱验证码），Kaptcha 仍因其轻量、易集成、可定制性强而在高并发场景下得到广泛使用。

**核心作用**：防止刷票和机器人请求，避免自动化脚本批量提交。

**选型理由**：

1，轻量级：零依赖、易集成，生成速度快

2，可定制：支持字体、颜色、干扰线等多种验证码样式

3，成熟稳定：社区广泛使用，性能可靠

### Gateway网关

```properties
# 路由转发
## id：路由标识，便于管理和调试。
## uri：负载均衡访问服务，使用 lb:// 表示通过注册中心发现对应服务。
## predicates：路由匹配规则，这里根据请求路径进行匹配，例如 /member/** 转发到 member 模块。
## http://127.0.0.1:xxxx用于本地调试直连模块,不经过注册中心

# 路由转发，将/member/...的请求转发了member模块
spring.cloud.gateway.routes[0].id=member
#spring.cloud.gateway.routes[0].uri=http://127.0.0.1:8001
spring.cloud.gateway.routes[0].uri=lb://member
spring.cloud.gateway.routes[0].predicates[0]=Path=/member/**

spring.cloud.gateway.routes[1].id=business
#spring.cloud.gateway.routes[1].uri=http://127.0.0.1:8002
spring.cloud.gateway.routes[1].uri=lb://business
spring.cloud.gateway.routes[1].predicates[0]=Path=/business/**

spring.cloud.gateway.routes[2].id=batch
#spring.cloud.gateway.routes[2].uri=http://127.0.0.1:8003
spring.cloud.gateway.routes[2].uri=lb://batch
spring.cloud.gateway.routes[2].predicates[0]=Path=/batch/**

```

```properties
# 跨域（CORS）配置

# 允许请求来源
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedOriginPatterns=*
# 允许携带的头信息
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedHeaders=*
# 允许的请求方式
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods=*
# 是否允许携带cookie
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowCredentials=true
# 跨域检测的有效期，会发起一个OPTION请求
spring.cloud.gateway.globalcors.cors-configurations.[/**].maxAge=3600
```

### 业务模块

```properties
# 服务端口与上下文路径
server.port=8002
server.servlet.context-path=/business
# 服务名称,用于注册中心注册服务的标识
spring.application.name=business
```

