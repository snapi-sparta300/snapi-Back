package stparta300.snapi.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient aiRestClient(
            @Value("${app.ai.base-url}") String baseUrl,
            @Value("${app.ai.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${app.ai.read-timeout-ms:9000}")   int readTimeoutMs
    ) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)       // ex) http://10.21.30.235:5000
                .requestFactory(factory) // 동기, 가장 단순한 타임아웃 방식
                .build();
    }
}