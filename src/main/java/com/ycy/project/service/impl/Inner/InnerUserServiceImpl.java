package com.ycy.project.service.impl.Inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ycy.project.common.ErrorCode;
import com.ycy.project.exception.BusinessException;
import com.ycy.project.mapper.InterfaceInfoMapper;
import com.ycy.project.mapper.UserMapper;

import com.ycy.ycyapicommon.model.entity.User;
import com.ycy.ycyapicommon.service.InnerUserService;
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
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getInvokeUser(String accessKey) {
        if (StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);
        return userMapper.selectOne(queryWrapper);
    }
}



