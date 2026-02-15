package com.openesign;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.openesign.mapper")
public class OpenESignApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenESignApplication.class, args);
    }
}
