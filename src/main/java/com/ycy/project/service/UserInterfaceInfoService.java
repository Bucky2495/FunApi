package com.ycy.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ycy.ycyapicommon.model.entity.UserInterfaceInfo;
import org.springframework.stereotype.Service;

@Service
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);
    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
