package com.shih.icecms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;

/**
* @author 1
* @description 针对表【matter】的数据库操作Mapper
* @createDate 2023-06-04 22:42:54
* @Entity com.shih.icecms.entity.Matter
*/
public interface MatterMapper extends BaseMapper<Matter> {
    Page<MatterDTO> listByPage(Page<MatterDTO> page, String matterId, String userId, int fullAction);
}




