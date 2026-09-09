package com.mym.healingenv;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        // ===== 数据库连接信息 =====
        String url = "jdbc:mysql://localhost:3306/yllypj?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456";

        // ===== 项目路径 =====
        String projectPath = System.getProperty("user.dir");
        String javaPath = projectPath + "/src/main/java";
        String xmlPath = projectPath + "/src/main/resources/mapper";

        // ===== 包配置 =====
        String parent = "com.mym.healingenv";
        String moduleName = "";  // 无子模块

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("mym")
                            .outputDir(javaPath)
                            .disableOpenDir();  // 生成完不自动打开文件夹
                })
                .packageConfig(builder -> {
                    builder.parent(parent)
                            .moduleName(moduleName)
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml, xmlPath));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("user,dimension,indicator_version,indicator,task,assignment,score,result,report,config")
                            // 表名 → 驼峰命名
                            .entityBuilder()
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .enableLombok()        // 使用 Lombok
                            .logicDeleteColumnName("deleted")  // 逻辑删除字段
                            .enableTableFieldAnnotation()      // 生成字段注解
                            .controllerBuilder()
                            .enableRestStyle();     // 生成 @RestController
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();

        System.out.println("代码生成完成！");
    }
}