package com.shih.icecms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@MapperScan({"com.shih.icecms.mapper"})
public class IceCmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(IceCmsApplication.class, args);
	}

}
