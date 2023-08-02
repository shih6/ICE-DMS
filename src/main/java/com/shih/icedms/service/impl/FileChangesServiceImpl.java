package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.entity.FileChanges;
import com.shih.icedms.service.FileChangesService;
import com.shih.icedms.mapper.FileChangesMapper;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【file_changes】的数据库操作Service实现
* @createDate 2023-05-03 20:46:35
*/
@Service
public class FileChangesServiceImpl extends ServiceImpl<FileChangesMapper, FileChanges>
    implements FileChangesService{

}




