package com.shih.icedms;

import com.gitee.starblues.loader.launcher.SpringBootstrap;
import com.gitee.starblues.loader.launcher.SpringMainBootstrap;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties
@MapperScan({"com.shih.icedms.mapper"})
public class IceDmsApplication implements SpringBootstrap {

	public static void main(String[] args) {
		SpringMainBootstrap.launch(IceDmsApplication.class, args);
	}

	@Override
	public void run(String[] args) throws Exception {
		SpringApplication.run(IceDmsApplication.class, args);
	}

}
