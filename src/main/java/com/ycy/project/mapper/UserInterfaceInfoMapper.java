package com.ycy.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ycy.ycyapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author 22118
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-04-18 22:49:57
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo>  listTopInvokeInterfaceInfo(int a);

}




