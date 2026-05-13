package com.chump.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.SessionFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;
import ua_parser.Parser;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
@ComponentScan(
        basePackages = "com.chump",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = RestController.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class)
        }
)
public class RootConfig {

        @Bean
        public PlatformTransactionManager transactionManager(SessionFactory factory) {
            return new HibernateTransactionManager(factory); // Управляет сессиями
        }

        @Bean
        public ObjectMapper objectMapper() {
            return JsonMapper.builder()
                        .addModule(new JavaTimeModule()) // Для поддержки Instant, LocalDate
                        .addModule(new JtsModule()) // Для поддержки Point, LineString, Polygon
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Instant как даты, а не как числа
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // Для точности передачи координат
                        .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                        .build();
        }

        @Bean
        public Parser parser() {
            return new Parser();
        }

        @Bean
        public GeometryFactory geometryFactory() {
            return new GeometryFactory(new PrecisionModel(), 4326);
        }
}
