package com.spring.security.web.config;

import com.spring.security.web.model.dto.PagedModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration
@EnableJpaAuditing
public class CommonWebConfig {

    @Bean
    public JacksonModule jacksonModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Page.class, new ValueSerializer<>() {
            @Override
            public void serialize(Page page, JsonGenerator gen, SerializationContext context) throws JacksonException {
                context.writeValue(gen, new PagedModel<>((Page<?>) page));
            }
        });
        return simpleModule;
    }
}
