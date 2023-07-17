package com.ycy.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ycy.ycyapicommon.model.entity.InterfaceInfo;


/**
* @author 22118
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-03-27 13:42:46
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    public void validInterfaceInfo(InterfaceInfo  interfaceInfo, boolean add);

}
