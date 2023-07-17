package com.ycy.project.controller;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ycy.project.annotation.AuthCheck;
import com.ycy.project.common.*;
import com.ycy.project.constant.CommonConstant;
import com.ycy.project.exception.BusinessException;
import com.ycy.project.mapper.UserInterfaceInfoMapper;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.ycy.project.model.enums.InterfaceStatusEnum;
import com.ycy.project.model.vo.InterfaceInfoVO;
import com.ycy.project.service.InterfaceInfoService;
import com.ycy.project.service.UserService;
import com.ycy.ycyapiclientsdk.client.YcyApiClient;
import com.ycy.ycyapicommon.model.entity.InterfaceInfo;
import com.ycy.ycyapicommon.model.entity.User;
import com.ycy.ycyapicommon.model.entity.UserInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口管理
 *
 * @author yupi
 */
/**
 * 分析控制器
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(interfaceInfoVOList);
    }
}
