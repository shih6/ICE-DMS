package com.shih.icedms.config;


import com.baomidou.mybatisplus.extension.ddl.SimpleDdl;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MysqlDdl extends SimpleDdl {

    /**
     * 执行 SQL 脚本方式
     */
    @Override
    public List<String> getSqlFiles() {
        return Arrays.asList(
                // 测试存储过程
                "db/setting_table_init.sql"
        );
    }
}