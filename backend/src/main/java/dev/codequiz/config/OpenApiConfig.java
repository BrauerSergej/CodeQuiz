package dev.codequiz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Настраивает заголовок/описание, которые видны сверху на странице Swagger UI
// (http://localhost:8082/swagger-ui.html). Сама генерация документации по
// контроллерам происходит автоматически — этот класс только добавляет
// общую информацию об API.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI codequizOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Codequiz API")
                        .description("REST API для квиз-приложения по программированию")
                        .version("0.0.1"));
    }
}