package org.apereo.cas.web;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasYamlHttpMessageConverter}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class CasYamlHttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    public CasYamlHttpMessageConverter() {
        super(JacksonObjectMapperFactory.builder().defaultTypingEnabled(true)
            .jsonFactory(new YAMLFactory()).build().toObjectMapper(),
            new MediaType("application", "vnd.cas.services+yaml"));
        setPrettyPrint(true);
        setDefaultCharset(StandardCharsets.UTF_8);
    }
}
