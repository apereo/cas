package org.apereo.cas.services.util;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceJsonSerializer extends BaseJacksonSerializer<RegisteredService> {

    @Serial
    private static final long serialVersionUID = 7645698151115635245L;

    public RegisteredServiceJsonSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext, RegisteredService.class);
    }
    
    @Override
    public boolean supports(final File file) {
        try {
            val content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
            return supports(content);
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean supports(final String content) {
        return content.contains(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
    }
    
    @Override
    public List<MediaType> getContentTypes() {
        return List.of(MediaType.APPLICATION_JSON);
    }

    @Override
    protected boolean isLenient() {
        return true;
    }
}
