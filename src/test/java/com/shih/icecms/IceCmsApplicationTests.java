package com.shih.icecms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.User;
import com.shih.icecms.service.MatterPermissionsService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.service.UsersService;
import com.shih.icecms.utils.MinioUtil;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class IceCmsApplicationTests {
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
