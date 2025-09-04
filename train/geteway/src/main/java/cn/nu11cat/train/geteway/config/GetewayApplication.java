package cn.nu11cat.train.geteway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan("cn.nu11cat")
public class GetewayApplication {
    private static final Logger LOG = LoggerFactory.getLogger(GetewayApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GetewayApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("启动成功！！");
        LOG.info("网关地址: \thttp://127.0.0.1:{}", env.getProperty("server.port"));
    }

}
