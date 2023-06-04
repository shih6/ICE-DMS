package com.shih.icecms.service;

import com.shih.icecms.dto.History;
import com.shih.icecms.entity.FileHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 1
* @description 针对表【file_history】的数据库操作Service
* @createDate 2023-05-03 20:45:36
*/
public interface FileHistoryService extends IService<FileHistory> {
    List<History> GetHistoryByFileId(String fileId);
}
