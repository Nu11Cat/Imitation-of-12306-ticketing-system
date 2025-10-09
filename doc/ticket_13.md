### 其他

进入指定目录

```shell
D:;cd D:\study\Nu11Cat\Imitation-of-12306-ticketing-system\
```

```
cd D:\study\Nu11Cat\Imitation-of-12306-ticketing-system\rocketmq-all-4.9.5-bin-release/bin
```

启动客户端前端

```shell
cd web; npm run web-dev 
```

启动管理台前端

```shell
cd admin; npm run admin-dev
```

单机模式启动 Nacos 服务

```shell
cd nacos/bin; .\startup.cmd -m standalone
```

启动 Seata 服务

```shell
cd seata/bin; .\seata-server.bat
```

运行 Sentinel 控制台

```she
java -jar sentinel-dashboard-1.8.6.jar
```

启动 RocketMQ 的命名服务

```shell
cd rocketmq-all-4.9.5-bin-release/bin; start mqnamesrv.cmd
```

启动 RocketMQ 的 Broker 服务，并指定连接的 NameServer 地址为本地 127.0.0.1:9876

```shell
cd rocketmq-all-4.9.5-bin-release/bin; .\mqbroker.cmd -n 127.0.0.1:9876
```

（清空D:\rocketmq-data\store，管理员身份cmd）

```shell
cd rocketmq-all-4.9.5-bin-release/bin; mqbroker.cmd -n 127.0.0.1:9876 -c ..\conf\broker.conf
```

关闭指定端口的服务(idea崩溃闪退导致错误退出服务)

```shell
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8000') do taskkill /PID %a /F
```

```shell
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8001') do taskkill /PID %a /F
```

```shell
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8002') do taskkill /PID %a /F
```

```shell
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8003') do taskkill /PID %a /F
```

压测

```shell
cd D:\study\Nu11Cat\yace
```

```shell
java -jar -Xms2g -Xmx3g train-business-0.0.1-SNAPSHOT.jar
java -jar -Xms2g -Xmx3g train-member-0.0.1-SNAPSHOT.jar
java -jar -Xms1g -Xmx2g gateway-0.0.1-SNAPSHOT.jar
```

```shell
jmeter -n -t D:/study/Nu11Cat/yace/train.jmx -l D:/study/Nu11Cat/yace/jmeter/test_result/result.txt -e -o D:/study/Nu11Cat/yace/jmeter/test_report

D:\study\Nu11Cat\Imitation-of-12306-ticketing-system\apache-jmeter-5.6.3\bin\jmeter.bat -n -t D:/study/Nu11Cat/yace/train.jmx -l D:/study/Nu11Cat/yace/jmeter/test_result/result.txt -e -o D:/study/Nu11Cat/yace/jmeter/test_report
```

```shell
jmeter -n -t [测试计划文件路径] -l [测试结果文件路径] -e -o [web报告保存路径]
```





























