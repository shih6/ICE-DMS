package com.shih.icedms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shih.icedms.dto.MatterDTO;
import com.shih.icedms.entity.Matter;

import java.util.List;

/**
* @author 1
* @description 针对表【matter】的数据库操作Mapper
* @createDate 2023-06-04 22:42:54
* @Entity com.shih.icedms.entity.Matter
*/
public interface MatterMapper extends BaseMapper<Matter> {
    Page<MatterDTO> list(Page<MatterDTO> page, String matterId, String userId, int fullAction);
    List<MatterDTO> list(String matterId, String userId, Integer type, int fullAction);
    Page<MatterDTO> listSearch(Page<MatterDTO> page,String matterName, String userId, int fullAction);
    MatterDTO getMatterDtoById(String matterId,String userId, int fullAction);
}




