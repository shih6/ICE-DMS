package com.shih.icecms;

import com.shih.icecms.config.MinioConfig;
import com.shih.icecms.utils.MinioUtil;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class IceCmsApplicationTests {
	@Autowired
	private MinioUtil minioUtil;
	@Autowired
	private MinioConfig prop;

	@Test
	void contextLoads() throws IOException, MinioException {
//		minioUtil.upload(new URL("http://192.168.0.107:9000/cache/files/data/3a105ab1-ddcd-4af8-83e8-803386ce7fab1_9951/output.docx/output.docx?md5=oPZfQWHMfPpKb7S32rypWw&expires=1683130184&filename=output.docx"), "3a105ab1-ddcd-4af8-83e8-803386ce7fab1/2.doc");
//		minioUtil.upload(new URL("http://192.168.0.107:9000/cache/files/data/3a105ab1-ddcd-4af8-83e8-803386ce7fab1_9951/changes.zip/changes.zip?md5=f3hNxUXBJl65HgfbDm6Fkg&expires=1683130184&filename=changes.zip"), "3a105ab1-ddcd-4af8-83e8-803386ce7fab1/2.zip");
//		URL a=new URL("http://192.168.0.107:9000/cache/files/data/3a105ab1-ddcd-4af8-83e8-803386ce7fab1_9951/changes.zip/changes.zip?md5=f3hNxUXBJl65HgfbDm6Fkg&expires=1683130184&filename=changes.zip");
//		a.openConnection();
		minioUtil.listVersions("1.zip");
		System.out.println("");

	}

}
