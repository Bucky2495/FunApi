package com.ycy.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ycy.project.common.*;


import com.ycy.project.service.InterfaceInfoService;
import com.ycy.ycyapiclientsdk.client.YcyApiClient;
import com.ycy.project.annotation.AuthCheck;
import com.ycy.project.common.*;
import com.ycy.project.constant.CommonConstant;
import com.ycy.project.exception.BusinessException;

import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.ycy.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.ycy.project.model.enums.InterfaceStatusEnum;
import com.ycy.project.service.UserService;
import com.ycy.ycyapicommon.model.entity.InterfaceInfo;
import com.ycy.ycyapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 接口管理
 *
 * @author yupi
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private YcyApiClient ycyApiClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                            HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String description = interfaceInfoQuery.getDescription();
        String name = interfaceInfoQuery.getName();
        String method = interfaceInfoQuery.getMethod();
        // content 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        interfaceInfoQuery.setName(null);
        interfaceInfoQuery.setMethod(null);

        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(method), "method", method);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }

    // endregion
    /**
     * 上线接口
     *
     * @param interfaceIdRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/online")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest interfaceIdRequest,
                                                     HttpServletRequest request) {

        if (interfaceIdRequest == null || interfaceIdRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断接口是否存在
        long id  = interfaceIdRequest.getId();
        InterfaceInfo byId = interfaceInfoService.getById(id);
        if(byId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断接口是否能被调用
        com.ycy.ycyapiclientsdk.model.User user = new com.ycy.ycyapiclientsdk.model.User();
        user.setUsername("ycy");
        String username = ycyApiClient.getNameBypost(user);
        if(StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口验证失败");
        }
        //接口能调用之后，要改变接口的状态
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceStatusEnum.ONLINE.getValue());
        boolean results  = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(results);
    }
    /**
     * 下线接口
     *
     * @param interfaceIdRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/offline")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest interfaceIdRequest,
                                                     HttpServletRequest request) {
        if (interfaceIdRequest == null || interfaceIdRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断接口是否存在
        long id  = interfaceIdRequest.getId();
        InterfaceInfo byId = interfaceInfoService.getById(id);
        if(byId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //接口能调用之后，要改变接口的状态
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceStatusEnum.OFFLINE.getValue());
        boolean results  = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(results);

    }

    /**
     * 调用接口
     *
     * @param interfaceInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInvokeRequest,
                                                    HttpServletRequest request) {
        if (interfaceInvokeRequest == null || interfaceInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断接口是否存在
        String userRequestParams = interfaceInvokeRequest.getUserRequestParams();
        long id  = interfaceInvokeRequest.getId();
        InterfaceInfo byId = interfaceInfoService.getById(id);
        if(byId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(byId.getStatus() == InterfaceStatusEnum.OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口已经关闭");
        }
        //获得当前登录用户
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        YcyApiClient tempClient = new YcyApiClient(accessKey,secretKey);
//        Gson gson  = new Gson();
//        com.ycy.ycyapiclientsdk.model.User user = gson.fromJson(userRequestParams,com.ycy.ycyapiclientsdk.model.User.class);
//        System.out.println(user.getUsername());
//        String usernameByPost = tempClient.postUserClient(user);
        String interfaceInfoName = byId.getName();
        Object result = reflectionInterface(YcyApiClient.class, interfaceInfoName, userRequestParams, accessKey, secretKey);
        return ResultUtils.success(result);

    }
    public Object reflectionInterface(Class<?> reflectionClass, String methodName, String parameter, String accessKey, String secretKey) {
        //构造反射类的实例
        Object result = null;
        try {
            Constructor<?> constructor = reflectionClass.getDeclaredConstructor(String.class, String.class);
            //获取SDK的实例，同时传入密钥
            YcyApiClient tempClient = (YcyApiClient) constructor.newInstance(accessKey, secretKey);
            //获取SDK中所有的方法
            Method[] methods = tempClient.getClass().getMethods();
            //筛选出调用方法
            for (Method method : methods
            ) {
                if (method.getName().equals(methodName)) {
                    //获取方法参数类型
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Method method1;
                    if (parameterTypes.length == 0){
                        method1 = tempClient.getClass().getMethod(methodName);
                        return method1.invoke(tempClient);
                    }
                    method1 = tempClient.getClass().getMethod(methodName, parameterTypes[0]);
                    //getMethod，多参会考虑重载情况获取方法,前端传来参数是JSON格式转换为String类型
                    //参数Josn化
                    Gson gson = new Gson();
                    Object args = gson.fromJson(parameter, parameterTypes[0]);
                    return result = method1.invoke(tempClient, args);
                }
            }
        } catch (Exception e) {
            log.error("反射调用参数错误",e);
        }
        return result;
    }
}
