package com.shih.icecms;

import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.utils.MinioUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IceCmsApplicationTests {
	@Autowired
	private MinioUtil minioUtil;
	@Autowired
	private MinioConfig prop;
	@Test
	void contextLoads() {
	}

}
