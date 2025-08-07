package stparta300.snapi.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Autowired
    public void configureMessageConverter(MappingJackson2HttpMessageConverter converter) {
        List<MediaType> supportMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        converter.setSupportedMediaTypes(supportMediaTypes);
    }

    @Bean
    public OpenAPI SnapiAPI() {
        Info info = new Info()
                .title("Snapi Swagger")
                .description("Snapi server Swagger")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes("JWT TOKEN", new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .in(SecurityScheme.In.HEADER)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                );

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}