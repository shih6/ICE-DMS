package com.shih.icedms;

import com.shih.icedms.config.MinioConfig;
import com.shih.icedms.entity.User;
import com.shih.icedms.service.MatterPermissionsService;
import com.shih.icedms.service.MatterService;
import com.shih.icedms.service.UsersService;
import com.shih.icedms.utils.MinioUtil;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class IceDmsApplicationTests {
	@Autowired
	private MinioUtil minioUtil;
	@Autowired
	private MinioConfig prop;
	@Resource
	private MatterPermissionsService matterPermissionsService;
	@Resource
	private MatterService matterService;
	@Resource
	private UsersService usersService;

	@Test
	void contextLoads() throws IOException, MinioException {
		User user =usersService.getById("test");
		String matterId="root";
		matterService.getTree(matterId,user.getId());
	}



	public static void main(String[] args){


	}

}
