package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.dto.ChangesHistory;
import com.shih.icecms.dto.History;
import com.shih.icecms.dto.UserDTO;
import com.shih.icecms.entity.FileChanges;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.User;
import com.shih.icecms.mapper.FileHistoryMapper;
import com.shih.icecms.service.FileChangesService;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.service.UsersService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 1
 * @description 针对表【file_history】的数据库操作Service实现
 * @createDate 2023-05-03 20:30:37
 */
@Service
public class FileHistoryServiceImpl extends ServiceImpl<FileHistoryMapper, FileHistory>
        implements FileHistoryService {
    @Resource
    private UsersService usersService;
    @Resource
    private FileChangesService fileChangesService;
    @Resource
    private MatterService matterService;

    public List<History> GetOnlyOfficeHistoryByFileId(String fileId) {
        List<FileHistory> fileHistories = list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId, fileId).orderBy(true,false,FileHistory::getCreated));
        List<History> histories = new ArrayList<>();
        for (FileHistory fileHistory : fileHistories) {
            User user = usersService.getById(fileHistory.getUserId());
            History history = new History(fileHistory.getServerVersion(), fileHistory.getDocKey()+fileHistory.getVersion(), fileHistory.getVersion(), fileHistory.getCreated().toString(), new UserDTO(user.getId(), user.getActualName()), null,null, new ArrayList<>());
            history.setUrl("http://192.168.0.112:8080/onlyoffice/matter/downloadForOnlyOffice?matterId="+fileHistory.getMatterId()+"&version="+fileHistory.getVersion());
            if(StringUtils.hasText(fileHistory.getChangesObjectName())){
                history.setChangesUrl("http://192.168.0.112:8080/downloadByObjectName?objectName="+ UriEncoder.encode(fileHistory.getChangesObjectName()));
            }
            for (FileChanges changes : fileChangesService.list(new LambdaQueryWrapper<FileChanges>().eq(FileChanges::getFileHistoryId, fileHistory.getId()))) {
                user = usersService.getById(changes.getUserId());
                history.getChanges().add(new ChangesHistory(changes.getCreated().toString(), new UserDTO(user.getId(), user.getActualName())));
            }
            histories.add(history);
        }

        return histories;
    }
    public List<FileHistory> getFileHistoryByMatterId(String matterId){
        return list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId,matterId).orderBy(true,false,FileHistory::getCreated));
    }
}




