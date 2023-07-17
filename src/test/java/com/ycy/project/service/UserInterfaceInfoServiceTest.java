package com.ycy.project.service;

import com.ycy.ycyapicommon.service.InnerUserInterfaceInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
@SpringBootTest
public class UserInterfaceInfoServiceTest {
    @Resource
    private InnerUserInterfaceInfoService userInterfaceInfoService;

    @Test
    public void invokeCount() {
        Boolean result = userInterfaceInfoService.invokeCount(1L, 1L);
        Assertions.assertTrue(result);


    }
}