package com.chump.common.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class OpenApiController {

    private final Resource openApiSpec;

    public OpenApiController() {
        this.openApiSpec = new ClassPathResource("/openapi/openapi.yaml");
    }

    @GetMapping(value = "/openapi.yaml", produces = "application/yaml")
    @ResponseBody
    public void getSpec(HttpServletResponse response) throws IOException {
        response.setContentType("application/yaml");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setCharacterEncoding("UTF-8");

        try (InputStream in = openApiSpec.getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }
}