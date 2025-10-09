### 问题排查/解决

#### Spring boot 3.0整合RocketMQ不兼容的问题

**问题发现**：

在Spring boot 3.0整合RocketMQ的时候，引入依赖后启动报错：

```text
***************************
APPLICATION FAILED TO START
***************************

Description:

Field rocketMQTemplate in com.spt.message.service.MqProducerService required a bean of type 'org.apache.rocketmq.spring.core.RocketMQTemplate' that could not be found.

The injection point has the following annotations:
    - @org.springframework.beans.factory.annotation.Autowired(required=true)


Action:

Consider defining a bean of type 'org.apache.rocketmq.spring.core.RocketMQTemplate' in your configuration.
```

**问题分析：**

Springboot-3.0已经放弃了spring.plants自动装配，被/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports所取代

**问题解决：**

在resources下创建META-INF，然后在META-INF下创建文件：org.springframework.boot.autoconfigure.AutoConfiguration.imports

（创建完文件后，文件被扫描到，在Idea图标会变化，如果没有变说明没有生效，也可能是命名错误或者创建文件用的"."而不是"/"）

**更多解决方法参考**：[ISSUE #539\] Feat: support SpringBoot 3.x by imp2002 · Pull Request #541 · apache/rocketmq-spring](https://github.com/apache/rocketmq-spring/pull/541)

---

#### 使用Lombok时maven install 找不到符号的问题

**问题发现**：

在使用Lombok的`@Data`的时候，正确引入了依赖和下载插件，并且Idea也给了提示没有爆红，但是使用maven install就会一直提示找不到符号

**问题分析：**

在查找资料后，在IntelliJ IDEA，可以通过安装 Lombok 插件并在项目设置中启用 Lombok 支持。

在 “Settings”（设置）->“Plugins”（插件）中搜索并安装 Lombok 插件，然后在 “Settings”->“Build, Execution, Deployment”->“Compiler”->“Annotation Processors” 中启用 “Enable annotation processing”（启用注解处理）。

但是实际上这个插件下载比较新的Idea的时候就下载了，然后这个设置也是默认开启的。

后来了解到在idea VM 配置中添加	`-D jps.track.ap.dependencies=false`，很多人通过它解决，但是对我不适用

**问题排查：**

Lombok 通过 注解处理器在编译阶段生成源码（如 getter/setter）。

在 IDE 里直接启动项目时，IDE 内置了 Lombok 支持，所以可以正常运行。

但是 mvn install 或 mvn compile 是纯命令行编译，如果没有告诉 Maven 使用 Lombok 注解处理器，编译器就找不到 get/set 方法，因此报 “cannot find symbol”。
**问题解决：**

在父pom添加Maven 编译器插件专门为 Lombok 配置注解处理器即可

maven-compiler-plugin 的 <annotationProcessorPaths> 就是告诉 Maven：

这个编译器插件在编译时，要加载 Lombok 这个注解处理器。

这样 @Data、@Getter、@Setter 等注解就会在编译时生成对应方法。

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

#### Seata注解与 MySQL 保留字列名产生冲突的问题

`@GlobalTransactional` 与 MySQL 保留字列名在 分布式事务代理/SQL解析层 产生了冲突

**问题发现**：

两张座位表中有字段名“row”“col”，mybatis使用SQL的时候会因为sql使用了 MySQL 的保留关键字（如 `order`、`desc`、`group`），导致 SQL 语法错误，使用`@TableField("row")`可以解决，但是一旦开启Seata注解`@GlobalTransactional` 后在调用相关的方法的时候可以在日志看到Seata使用的sql是MySQL 的保留关键字导致报错

**问题分析：**

使用`@TableField("row")`后MyBatis-Plus会在生成的 SQL 中自动加反引号，但是`@GlobalTransactional`会生成自己的 SQL（如记录事务日志），不会自动加反引号，就算添加MyBatis-Plus的config配置也不会对Seata生效。

**问题解决：**

虽然可以通过改配置解决，但是直接改字段也不麻烦，一劳永逸。

```sql
ALTER TABLE train_seat 
    CHANGE `row` `row_index` varchar(2) NOT NULL COMMENT '行号',
    CHANGE `col` `col_index` varchar(1) NOT NULL COMMENT '列号';
```

---

#### 乐观锁未生效导致的高并发场景下的超卖问题

**问题发现与分析**：

mp内置的乐观锁插件通过数据库添加version字段，实体类添加字段和@version注解，添加配置类即可生效。但是如果没有在乐观锁冲突的时候抛出异常触发事务和Redis回滚，相当于没有这层防护。因为Redis 分布式锁只能保证操作过程串行化，无法保证数据库层最终一致性，尤其在读-改-写的情况下。所以高并发场景还是会超卖。

**问题解决：**

当乐观锁冲突的时候抛出异常触发事务和Redis回滚：

```java
boolean updated = dailyTrainSeatMapper.updateById(dailyTrainSeat) > 0;
if (!updated) {
    throw new BusinessException(BusinessExceptionEnum.Optimistic_Lock_Conflict);
}
```

---

#### 高并发场景下MQ消费端重复消费的问题

**问题发现：

当设置100线程/s执行5次后，发现，订单有500个，但是redis和mysql票少了503张，用户车票有503张，座位状态更改了503个。

**问题分析：**

因为订单生成是在请求到达拿到令牌之后，购票的逻辑都是在发送MQ之后，所以猜测是当乐观锁发生冲突抛出异常导致MQ重新消费（RocketMQ的默认方式），虽然本来就要优化做幂等，但是送上门来的.....

**问题解决：**

使用Redis做幂等，通过全局唯一的LOG_ID作为消息的标识，已消费过的消息的LOG_ID放到Redis，消费前做一个校验。

```java
String idempotentKey = "mq:doConfirm:idempotent:" + dto.getLogId();
Boolean firstConsume = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 10, TimeUnit.MINUTES);
if (Boolean.FALSE.equals(firstConsume)) {
    LOG.warn("重复消费消息，跳过执行，logId={}", dto.getLogId());
    return;
}
```

---







