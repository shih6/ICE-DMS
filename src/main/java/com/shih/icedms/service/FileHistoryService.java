package com.shih.icedms.service;

import com.shih.icedms.dto.History;
import com.shih.icedms.entity.FileHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 1
* @description 针对表【file_history】的数据库操作Service
* @createDate 2023-05-03 20:45:36
*/
public interface FileHistoryService extends IService<FileHistory> {
    List<History> GetOnlyOfficeHistoryByFileId(String fileId);

    List<FileHistory> getFileHistoryByMatterId(String matterId);
}
