package com.shih.icedms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties
@MapperScan({"com.shih.icedms.mapper"})
public class IceDmsApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		SpringApplication.run(IceDmsApplication.class, args);
	}

}
