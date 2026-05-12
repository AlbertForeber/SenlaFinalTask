package com.chump.test_config;

import com.chump.auth.service.JwtService;
import com.chump.common.security.JwtAuthenticationFilter;
import com.chump.user.service.query.UserQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class TestConfig implements WebMvcConfigurer {

    @Override
    public @Nullable Validator getValidator() {
        return new LocalValidatorFactoryBean();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());

        // Добавляем первым, как приоритетный
        converters.add(0, converter);
    }

    @Bean
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            ObjectMapper objectMapper
    ) {
        return new JwtAuthenticationFilter(jwtService, objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule()) // Для поддержки Instant, LocalDate
                .addModule(new JtsModule()) // Для поддержки Point, LineString, Polygon
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Instant как даты, а не как числа
                .build();
    }

    @Bean
    public UserQueryService userQueryService() {
        return Mockito.mock(UserQueryService.class);
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
