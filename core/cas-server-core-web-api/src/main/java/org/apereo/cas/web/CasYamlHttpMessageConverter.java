package org.apereo.cas.web;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * The CAS YAML media type.
     */
    public static final MediaType MEDIA_TYPE_CAS_YAML = new MediaType("application", "vnd.cas.services+yaml");

    /**
     * The YAML media type.
     */
    public static final MediaType MEDIA_TYPE_YAML = MediaType.parseMediaType("application/yaml");

    /**
     * The YAML media type.
     */
    public static final MediaType MEDIA_TYPE_YML = MediaType.parseMediaType("application/yml");

    private static final ObjectMapper YAML_OBJECT_MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .jsonFactory(new YAMLFactory())
        .build()
        .toObjectMapper();

    public CasYamlHttpMessageConverter() {
        super(YAML_OBJECT_MAPPER, MEDIA_TYPE_CAS_YAML, MEDIA_TYPE_YAML, MEDIA_TYPE_YML);
        setPrettyPrint(true);
        setDefaultCharset(StandardCharsets.UTF_8);
    }

    @Override
    public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
        return mediaType != null && getSupportedMediaTypes().contains(mediaType);
    }
}
