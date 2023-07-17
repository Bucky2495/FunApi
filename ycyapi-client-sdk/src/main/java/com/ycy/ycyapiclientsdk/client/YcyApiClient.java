package com.ycy.ycyapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;



import java.util.HashMap;
import java.util.Map;
import com.ycy.ycyapiclientsdk.model.User;

import static com.ycy.ycyapiclientsdk.utils.SignUtils.getSign;


public class YcyApiClient {
    private String accessKey;
    private String secretKey;
    private static final String GATEWAY_HOST = "http://localhost:8090";

    public YcyApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getClient(String name) {
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "ycy");

        String result= HttpUtil.get( GATEWAY_HOST + "/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String postClient(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", "ycy");

        String result= HttpUtil.post(  GATEWAY_HOST + "/api/name/", paramMap);
        System.out.println(result);
        return result;
    }


    /**
     * 封装请求头
     * @return
     */
    private Map<String,String> getHeader(String body) {
        Map<String,String> headers = new HashMap<>();
        headers.put("accessKey",accessKey);
        //headers.put("secretKey",secretKey); 密钥一定不能直接发送给后端
        //添加随机数参数防止重放
        headers.put("nonce", RandomUtil.randomNumbers(4));
        //添加用户参数
        headers.put("body","ycy");
        //添加时间戳
        headers.put("timestamp",String.valueOf(System.currentTimeMillis()/1000));
        //添加签名
        headers.put("sign",getSign("ycy",secretKey));
        return headers;

    }
    public String getNameBypost(User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse result = HttpRequest.post(  GATEWAY_HOST + "/api/name/user")
                .addHeaders(getHeader(json))
                .body(json)
                .execute();
        System.out.println(result.body());
        return result.body();
    }

    /**
     * 返回一段话
     */
    public String getOneSentance(User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse result = HttpRequest.get(  GATEWAY_HOST + "/api/name/sentence")
                .addHeaders(getHeader(json))
                .body(json)
                .execute();
        return result.body();
    }




}
