package com.ycy.ycyapiclientsdk;
/**
 * @Auther: ycy
 * @Date: 2023/4/10 23:37
 * @Description:
 */
import com.ycy.ycyapiclientsdk.client.YcyApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties("ycyapi.client")
@ComponentScan
public class YcyApiClientConfig {

        private String accessKey;

        private String secretKey;

        @Bean
        public YcyApiClient ycyApiClient() {
                return new YcyApiClient(accessKey, secretKey);

        }
}
