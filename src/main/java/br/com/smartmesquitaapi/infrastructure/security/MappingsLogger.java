package br.com.smartmesquitaapi.infrastructure.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class MappingsLogger {
    @Bean
    CommandLineRunner printMappings(RequestMappingHandlerMapping mapping) {
        return args -> mapping.getHandlerMethods()
                .forEach((key, value) -> System.out.println(key + " -> " + value));
    }
}
