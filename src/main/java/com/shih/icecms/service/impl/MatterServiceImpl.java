package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.dto.MatterDTO;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.mapper.MatterMapper;
import com.shih.icecms.service.MatterService;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【matter】的数据库操作Service实现
* @createDate 2023-06-04 21:16:46
*/
@Service
public class MatterServiceImpl extends ServiceImpl<MatterMapper, Matter>
    implements MatterService{
    public Page<MatterDTO> listByPage(String matterId, String userId, int pageNum, int pageSize){
        return baseMapper.listByPage(Page.of(pageNum,pageSize),matterId, userId, 31);
    }
}



