package com.shih.icedms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.entity.FileHistory;
import com.shih.icedms.entity.Matter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 1
* @description 针对表【matter】的数据库操作Service
* @createDate 2023-06-04 21:16:46
*/
public interface MatterService extends IService<Matter> {
    List<MatterDTO> list(String matterId, String userId,Integer type);
    Page<MatterDTO> listSearch(Page page,String matterName);
    Page<MatterDTO> listByPage(String matterId, String userId, int pageNum, int pageSize);
    MatterDTO getMatterDtoById(String matterId,String userId);
    MatterDTO uploadFile(MultipartFile multipartFile, String parentMatterId) throws Exception;
    Boolean deleteMatter(String matterId);
    MatterDTO getTree(String matterId,String userId);
    void getPath(String matterId, @NotNull List<String> outPut);
    void saveOrUpdateMatter(String userId, FileHistory newHistory, Matter matter, Integer version);
}
