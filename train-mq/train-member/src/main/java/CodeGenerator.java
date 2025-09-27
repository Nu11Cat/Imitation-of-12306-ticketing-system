import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;


public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/train-member?serverTimezone=Asia/Shanghai",
                        "train",
                        "123456"
                )
                // 全局配置
                .globalConfig(builder -> builder
                        .author("nu11cat")         // 作者
                        .enableSwagger()          // 开启 swagger 注解
                        .fileOverride()           // 覆盖已生成文件
                        .outputDir(System.getProperty("user.dir") + "/train-member/src/main/java") // 输出路径
                )
                // 包配置
                .packageConfig(builder -> builder
                        .parent("cn.nu11cat.train.member") // 父包
                        .entity("entity")
                        .service("service")
                        .serviceImpl("service.impl")
                        .mapper("mapper")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                System.getProperty("user.dir") + "/train-member/src/main/resources/mapper")) // mapper.xml 输出路径
                )
                // 策略配置
                .strategyConfig(builder -> builder
                        .addInclude("ticket") // 生成的表
                        .addTablePrefix("")      // 表前缀
                        .entityBuilder()
                        .enableLombok()
                        .controllerBuilder()
                        .enableRestStyle()
                )
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 freemarker 模板
                .execute();
    }
}
