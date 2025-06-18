package com.portfolio.food_delivery.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 보안 요구사항
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(apiInfo())
                .servers(getServers())
                .tags(getTags())
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(Arrays.asList(securityRequirement));
    }

    private Info apiInfo() {
        return new Info()
                .title("Food Delivery Platform API")
                .description("음식 배달 플랫폼 백엔드 API 문서")
                .version("1.0.0")
                .contact(new Contact()
                        .name("개발자 이름")
                        .email("developer@example.com")
                        .url("https://github.com/username/food-delivery"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private List<Server> getServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://api.fooddelivery.com")
                .description("Production Server");

        return Arrays.asList(localServer, productionServer);
    }

    private List<Tag> getTags() {
        return Arrays.asList(
                new Tag().name("Users").description("사용자 관련 API"),
                new Tag().name("Restaurants").description("레스토랑 관련 API"),
                new Tag().name("Menus").description("메뉴 관련 API"),
                new Tag().name("Orders").description("주문 관련 API"),
                new Tag().name("Cart").description("장바구니 관련 API"),
                new Tag().name("Payments").description("결제 관련 API"),
                new Tag().name("Reviews").description("리뷰 관련 API")
        );
    }
}