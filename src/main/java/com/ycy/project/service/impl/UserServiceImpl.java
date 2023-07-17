package com.ycy.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.ycy.project.common.BaseResponse;
import com.ycy.project.common.ResultUtils;
import com.ycy.project.common.SmsLimiter;
import com.ycy.project.model.dto.user.UserDTO;
import com.ycy.project.service.UserService;
import com.ycy.project.common.ErrorCode;
import com.ycy.project.exception.BusinessException;
import com.ycy.project.mapper.UserMapper;
import com.ycy.ycyapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ycy.project.constant.UserConstant.ADMIN_ROLE;
import static com.ycy.project.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务实现类
 *
 * @author yupi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SmsLimiter smsLimiter;



    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(user.getUserName());
            user.setSecretKey("123456");
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public String userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        //将此处的用户信息保存在redis中
        //request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //将user转为一个hashMap保存
        String token = UUID.randomUUID().toString(true);
        HashMap<String, String> UserMap = new HashMap<>();
        UserMap.put("id",user.getId().toString());
        UserMap.put("userName",user.getUserName());
        UserMap.put("userRole",user.getUserRole());
        String tokenKey = USER_LOGIN_STATE + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,UserMap);
        //设置有效期
        stringRedisTemplate.expire(tokenKey,30,TimeUnit.MINUTES);
        return token;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
//        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
//        if (currentUser == null || currentUser.getId() == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
        String token = request.getHeader("Authorization");

        String key = USER_LOGIN_STATE + token;

        Map<Object, Object>  currentUser1 = stringRedisTemplate.opsForHash().entries(key);
        if(currentUser1.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long Id = Long.parseLong((String)currentUser1.get("id"));



        // 从数据库查询（追求性能的话可以注释，直接走缓存）
         User currentUser = this.getById(Id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 生成图形验证码
     * @param request
     * @param response
     */
    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 随机生成 4 位验证码
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        // 定义图片的显示大小
        LineCaptcha lineCaptcha = null;
        lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30);
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "No-cache");
        // 在前端发送请求时携带captchaId，用于标识不同的用户。
        String signature = request.getHeader("signature");
        if (null == signature){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        try {
            // 调用父类的 setGenerator() 方法，设置验证码的类型
            lineCaptcha.setGenerator(randomGenerator);
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature,lineCaptcha.getCode());
            // 关闭流
            response.getOutputStream().close();
            //将对应的验证码存入redis中去，2分钟后过期
            redisTemplate.opsForValue().set(CAPTCHA_PREFIX+signature,lineCaptcha.getCode(),4, TimeUnit.MINUTES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送验证码
     * @param mobile
     * @return
     */
    @Override
    public BaseResponse captcha(String mobile) throws NumberParseException {
        if (mobile == null ){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        //验证手机号的合法性
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        com.google.i18n.phonenumbers.Phonenumber.PhoneNumber parsedPhoneNumber = phoneNumberUtil.parse(mobile, null);
        if(!phoneNumberUtil.isValidNumber(parsedPhoneNumber)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "手机号非法");
        }
        int code = (int)((Math.random() * 9 + 1) * 10000);
        // 使用redis来存储手机号和验证码 ，同时使用令牌桶算法来实现流量控制
        boolean sendSmsAuth = smsLimiter.sendSmsAuth(mobile, String.valueOf(code));
        if(!sendSmsAuth){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送频率过高，请稍后再试");
        }
        log.info("发送验证码成功---->手机号为{}，验证码为{}",mobile,code);
        return ResultUtils.success("发送成功");
    }
}




