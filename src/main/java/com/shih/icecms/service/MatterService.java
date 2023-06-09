package com.shih.icecms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;

/**
* @author 1
* @description 针对表【matter】的数据库操作Service
* @createDate 2023-06-04 21:16:46
*/
public interface MatterService extends IService<Matter> {
    Page<MatterDTO> listByPage(String matterId, String userId, int pageNum, int pageSize);
}
