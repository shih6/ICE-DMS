package com.shih.icecms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shih.icecms.dto.ChangesHistory;
import com.shih.icecms.dto.History;
import com.shih.icecms.dto.UserDTO;
import com.shih.icecms.entity.FileChanges;
import com.shih.icecms.entity.FileHistory;
import com.shih.icecms.entity.Matter;
import com.shih.icecms.entity.Users;
import com.shih.icecms.mapper.FileHistoryMapper;
import com.shih.icecms.service.FileChangesService;
import com.shih.icecms.service.FileHistoryService;
import com.shih.icecms.service.MatterService;
import com.shih.icecms.service.UsersService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
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

    public List<History> GetHistoryByFileId(String fileId) {
        List<FileHistory> fileHistories = list(new LambdaQueryWrapper<FileHistory>().eq(FileHistory::getMatterId, fileId));
        List<History> histories = new ArrayList<>();
        Dictionary<Integer, Matter> matterHashtable = new Hashtable<>();
        for (Matter file : matterService.list(new LambdaQueryWrapper<Matter>().eq(Matter::getFileId, fileId))) {
            if (file.getType() == 1) matterHashtable.put(file.getVersion(), file);
        }
        for (FileHistory fileHistory : fileHistories) {
            Users users = usersService.getById(fileHistory.getUserId());
            Matter file = matterHashtable.get(fileHistory.getVersion());
            History history = new History(fileHistory.getServerVersion(), fileHistory.getDocKey()+fileHistory.getVersion(), fileHistory.getVersion(), fileHistory.getCreated().toString(), new UserDTO(users.getId(), users.getActualName()), null,null, new ArrayList<>());
            history.setUrl("http://192.168.0.112:8080/download?fileId="+file.getFileId()+"&fileVersion="+file.getVersion()+"&fileType="+file.getType());
            if(StringUtils.hasText(fileHistory.getChangesObjectName())){
                history.setChangesUrl("http://192.168.0.112:8080/downloadByObjectName?objectName="+fileHistory.getChangesObjectName());
            }
            for (FileChanges changes : fileChangesService.list(new LambdaQueryWrapper<FileChanges>().eq(FileChanges::getFileHistoryId, fileHistory.getId()))) {
                users = usersService.getById(changes.getUserId());
                history.getChanges().add(new ChangesHistory(changes.getCreated().toString(), new UserDTO(users.getId(), users.getActualName())));
            }
            histories.add(history);
        }

        return histories;
    }

}




