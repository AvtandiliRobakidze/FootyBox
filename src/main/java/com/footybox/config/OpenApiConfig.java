package com.footybox.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI footyBoxOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FootyBox API")
                        .version("0.1.0")
                        .description("Football match diary, review, and archive API."));
    }
}
