package cn.nu11cat.train.member.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${test.nacos}")
    private String testNacos;

   // @Autowired
    //Environment environment;

    @GetMapping("/hello")
    public String hello() {
        //String port = environment.getProperty("local.server.port");
        //return String.format("Hello %s! 端口：%s", testNacos, port);
        return String.format("Hello %s!", testNacos);
    }
}
