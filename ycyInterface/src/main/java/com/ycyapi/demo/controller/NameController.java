package com.ycyapi.demo.controller;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.ycy.ycyapiclientsdk.model.User;
import com.ycy.ycyapiclientsdk.utils.SignUtils;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByget(String name) {

        return "Get 你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameBypost(@RequestParam String name) {
        return "Post 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getNameBypost(@RequestBody User user, HttpServletRequest httpServletRequest) {
        String accessKey = httpServletRequest.getHeader("accessKey");
        String nonce = httpServletRequest.getHeader("nonce");
        String timestamp = httpServletRequest.getHeader("timestamp");
        String sign = httpServletRequest.getHeader("sign");
        String body = httpServletRequest.getHeader("body");
//        String secretKey = httpServletRequest.getHeader("secretKey");
        // todo 实际上应该去数据库查询是否已经分配给用户
        if(!accessKey.equals(user.getUsername())) {
            throw new RuntimeException("无权限");
        }
        if(  Long.parseLong(nonce) > 10000) {
            throw new RuntimeException("无权限");
        }
        //此处应该事服务端根据用户信息取数据库查密钥
        String serverSign = SignUtils.getSign(body,"123456");
        if(!sign.equals(serverSign)) {
            throw new RuntimeException("无权限");
        }
        return "Post 用户名为" + user.getUsername();
    }
    @GetMapping("/sentence")
    public String getOneSentance( HttpServletRequest httpServletRequest) {
        String accessKey = httpServletRequest.getHeader("accessKey");
        String nonce = httpServletRequest.getHeader("nonce");
        String timestamp = httpServletRequest.getHeader("timestamp");
        String sign = httpServletRequest.getHeader("sign");
        String body = httpServletRequest.getHeader("body");

//        String secretKey = httpServletRequest.getHeader("secretKey");
        // todo 实际上应该去数据库查询是否已经分配给用户
//        if(!accessKey.equals(user.getUsername())) {
//            throw new RuntimeException("无权限");
//        }
        if(  Long.parseLong(nonce) > 10000) {
            throw new RuntimeException("无权限");
        }
        //此处应该事服务端根据用户信息取数据库查密钥
        String serverSign = SignUtils.getSign(body,"123456");
        if(!sign.equals(serverSign)) {
            throw new RuntimeException("无权限");
        }
        String result = HttpUtil.get("https://v.api.aa1.cn/api/yiyan/index.php"
                , CharsetUtil.CHARSET_UTF_8);


        if (StrUtil.isBlank(result)){

            return "此接口发生了意外55！！";
        }
        return result;
    }
}
