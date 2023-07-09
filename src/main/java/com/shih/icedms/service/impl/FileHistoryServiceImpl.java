package com.shih.icedms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icedms.dto.ChangesHistory;
import com.shih.icedms.dto.History;
import com.shih.icedms.dto.UserDTO;
import com.shih.icedms.entity.FileChanges;
import com.shih.icedms.entity.FileHistory;
import com.shih.icedms.entity.User;
import com.shih.icedms.mapper.FileHistoryMapper;
import com.shih.icedms.service.FileChangesService;
import com.shih.icedms.service.FileHistoryService;
import com.shih.icedms.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private UsersService usersService;
    private FileChangesService fileChangesService;
    @Autowired
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }
    @Autowired
    public void setFileChangesService(FileChangesService fileChangesService) {
        this.fileChangesService = fileChangesService;
    }
    @Value("${setting.documentServer.fileStorageServerHost}")
    private String fileStorageServerHost;
    public List<History> GetOnlyOfficeHistoryByFileId(String fileId) {
        List<FileHistory> fileHistories = list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId, fileId).orderBy(true,false,FileHistory::getCreated));
        List<History> histories = new ArrayList<>();
        for (FileHistory fileHistory : fileHistories) {
            User user = usersService.getById(fileHistory.getUserId());
            History history = new History(fileHistory.getServerVersion(), fileHistory.getDocKey()+fileHistory.getVersion(), fileHistory.getVersion(), fileHistory.getCreated().toString(), new UserDTO(user.getId(), user.getActualName()), null,null, new ArrayList<>());
            history.setUrl(fileStorageServerHost +"/onlyoffice/downloadForOnlyOffice?matterId="+fileHistory.getMatterId()+"&version="+fileHistory.getVersion());
            if(StringUtils.hasText(fileHistory.getChangesObjectName())){
                history.setChangesUrl(fileStorageServerHost +"/onlyoffice/downloadChanges?matterId="+ fileHistory.getMatterId()+"&version="+fileHistory.getVersion());
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




