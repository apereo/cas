package org.apereo.cas.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * This is {@link CasCoreConfigurationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public final class CasCoreConfigurationUtils {

    /**
     * Load yaml properties map.
     *
     * @param resource the resource
     * @return the map
     */
    public static Map<String, Object> loadYamlProperties(final Resource... resource) {
        val factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(resource);
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        return (Map) factory.getObject();
    }

    /**
     * Encode all CAS properties as a Map.
     *
     * @param properties the properties
     * @return the map
     */
    @SneakyThrows
    public static Map<String, Object> asMap(final Serializable properties) {
        try (val writer = new StringWriter()) {
            val mapper = new YAMLMapper();
            val module = new SimpleModule();
            module.addSerializer(Resource.class, new ResourceSerializer());
            mapper.registerModule(module);
            mapper.writeValue(writer, properties);
            val resource = new ByteArrayResource(writer.toString().getBytes(StandardCharsets.UTF_8));
            return CasCoreConfigurationUtils.loadYamlProperties(resource);
        }
    }

    private static class ResourceSerializer extends StdSerializer<Resource> {
        private static final long serialVersionUID = 7971411664567411958L;

        ResourceSerializer() {
            this(null);
        }

        ResourceSerializer(final Class<Resource> t) {
            super(t);
        }

        @Override
        public void serialize(final Resource value, final JsonGenerator jgen,
                              final SerializerProvider provider) throws IOException {
            if (value instanceof ClassPathResource) {
                jgen.writeString(CLASSPATH_URL_PREFIX + value.getFilename());
            } else {
                jgen.writeString(value.getURI().toString());
            }
        }
    }
}
