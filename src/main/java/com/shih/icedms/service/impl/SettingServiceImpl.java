package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.entity.Setting;
import com.shih.icedms.service.SettingService;
import com.shih.icedms.mapper.SettingMapper;
import org.springframework.stereotype.Service;

/**
* @author chigu
* @description 针对表【setting】的数据库操作Service实现
* @createDate 2023-08-29 20:49:42
*/
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting>
    implements SettingService{

}




