package com.shih.icedms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shih.icedms.dto.CreateDto;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.entity.FileHistory;
import com.shih.icedms.entity.Matter;
import io.minio.errors.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.ConnectException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
* @author 1
* @description 针对表【matter】的数据库操作Service
* @createDate 2023-06-04 21:16:46
*/
public interface MatterService extends IService<Matter> {
    List<MatterDTO> listOfDto(String userId,Integer type);
    Page<MatterDTO> listSearch(Page page,String matterName);
    MatterDTO getMatterDtoById(String matterId,String userId);
    MatterDTO uploadFile(MultipartFile multipartFile, String parentMatterId) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;
    MatterDTO create(CreateDto createDto) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException;
    Boolean deleteMatter(String matterId) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException;
    List<String> deleteMatters(String matterIds) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;
    MatterDTO getTreeV2(String userId,int type);
    void saveOrUpdateMatter(String userId, FileHistory newHistory, Matter matter, Integer version);
    boolean move(String matterId,String target);

    String getMatterAuthToken(String matterId,String version, HttpServletResponse res);
}
