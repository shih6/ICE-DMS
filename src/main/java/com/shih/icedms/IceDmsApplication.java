package com.shih.icedms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@MapperScan({"com.shih.icedms.mapper"})
public class IceDmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(IceDmsApplication.class, args);
	}

}
