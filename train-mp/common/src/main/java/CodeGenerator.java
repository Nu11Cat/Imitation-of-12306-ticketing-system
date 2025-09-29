import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;


public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/train-business?serverTimezone=Asia/Shanghai",
                        "train",
                        "123456"
                )
                // 全局配置
                .globalConfig(builder -> builder
                        .author("nu11cat")         // 作者
                        //.enableSwagger()          // 开启 swagger 注解
                        .fileOverride()           // 覆盖已生成文件
                        .outputDir(System.getProperty("user.dir") + "/train-business/src/main/java") // 输出路径
                )
                // 包配置
                .packageConfig(builder -> builder
                        .parent("cn.nu11cat.train.business") // 父包
                        .entity("entity")
                        .service("service")
                        .serviceImpl("service.impl")
                        .mapper("mapper")
                        //.controller("controller")
                        .controller("controller.admin")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                System.getProperty("user.dir") + "/train-business/src/main/resources/mapper")) // mapper.xml 输出路径
                )
                // 策略配置
                .strategyConfig(builder -> builder
                        .addInclude("sk_token") // 生成的表
                        .addTablePrefix("")      // 表前缀
                        .entityBuilder()
                        .enableLombok()
                        .controllerBuilder()
                        .enableRestStyle()
                        .formatFileName("%sAdminController") // 改成 AdminController
                        .serviceBuilder()
                        .formatServiceFileName("%sService")       // 关键：接口不加 I 前缀
                        .formatServiceImplFileName("%sServiceImpl") // 实现类正常
                )
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 freemarker 模板
                .execute();
    }
}
