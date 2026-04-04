package simply.Finsight_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Slf4j
public class SwaggerConfig {

    @Bean
    public OpenAPI myCustomConfig() {
        log.info("Initializing Swagger OpenAPI configuration for Finsight Backend...");

        return new OpenAPI()
                .info(new Info()
                        .title("Finsight APIs")
                        .description("Finance Data Processing and Access Control Backend - Developed by Hemant")
                        .version("1.0.0"))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Local host")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token in the format: <token>")
                ));
    }
}
