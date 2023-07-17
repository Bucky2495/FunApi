package com.ycy.project.service.impl.Inner;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ycy.project.common.ErrorCode;
import com.ycy.project.exception.BusinessException;
import com.ycy.project.mapper.InterfaceInfoMapper;
import com.ycy.project.model.entity.InterfaceInfo;
import com.ycy.project.service.InterfaceInfoService;
import com.ycy.project.service.UserInterfaceInfoService;
import com.ycy.ycyapicommon.service.InnerUserInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 22118
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-03-27 13:42:46
*/
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }
}


