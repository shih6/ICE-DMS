package com.shih.icecms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 1
* @description 针对表【matter】的数据库操作Service
* @createDate 2023-06-04 21:16:46
*/
public interface MatterService extends IService<Matter> {
    List<MatterDTO> list(String matterId, String userId,Integer type);
    Page<MatterDTO> listByPage(String matterId, String userId, int pageNum, int pageSize);
    MatterDTO getMatterDtoById(String matterId,String userId);
    MatterDTO uploadFile(MultipartFile multipartFile, String parentMatterId);
    Boolean deleteMatter(String matterId);
    MatterDTO getTree(String matterId,String userId);
    void getPath(String matterId, @NotNull List<String> outPut);
}
