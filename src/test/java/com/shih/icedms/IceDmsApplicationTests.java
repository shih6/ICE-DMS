package com.shih.icedms;

import com.shih.icedms.config.MinioConfig;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.entity.User;
import com.shih.icedms.service.MatterPermissionsService;
import com.shih.icedms.service.MatterService;
import com.shih.icedms.service.UsersService;
import com.shih.icedms.utils.MinioUtil;
import io.jsonwebtoken.lang.Assert;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

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

	@RepeatedTest(5)
	void getTreeV2(){
		List<User> list = usersService.list();
		matterService.getTreeV2("admin", 1);
	}
	@Test
	void compare(){
		List<User> list = usersService.list();
		for (User user : list) {
			MatterDTO treeV2 = matterService.getTreeV2(user.getId(), 0);
		}
	}

}
