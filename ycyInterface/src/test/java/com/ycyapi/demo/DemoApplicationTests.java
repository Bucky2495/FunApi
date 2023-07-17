package com.ycyapi.demo;

import com.ycy.ycyapiclientsdk.client.YcyApiClient;
import com.ycy.ycyapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class DemoApplicationTests {
    @Resource
    private YcyApiClient ycyApiClient;

    @Test
    void contextLoads() {
        String result = ycyApiClient.getClient("ycy");
        String result2 = ycyApiClient.postClient("ycy");
        String result3 = ycyApiClient.postUserClient(new User("ycy"));
        System.out.println(result);
        System.out.println(result2);
        System.out.println(result3);
    }

}
