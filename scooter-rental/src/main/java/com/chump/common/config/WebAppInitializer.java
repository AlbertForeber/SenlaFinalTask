package com.chump.common.config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected @Nullable Class<?>[] getRootConfigClasses() {
        return new Class[] {RootConfig.class};
    }

    @Override
    protected @Nullable Class<?>[] getServletConfigClasses() {
        return new Class[0];
    }

    @Override
    protected String @NotNull [] getServletMappings() {
        return new String[] {"/"};
    }
}
