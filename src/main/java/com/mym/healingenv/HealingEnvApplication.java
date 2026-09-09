package com.mym.healingenv;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mym.healingenv.mapper")
public class HealingEnvApplication {

    public static void main(String[] args) {

        SpringApplication.run(HealingEnvApplication.class, args);
    }

}
