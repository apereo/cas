package org.apereo.cas.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
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
     * @param properties     the properties
     * @param filterProvider the filter provider
     * @return the map
     */
    @SneakyThrows
    public static Map<String, Object> asMap(final Serializable properties,
                                            final FilterProvider filterProvider) {
        try (val writer = new StringWriter()) {
            val mapper = new YAMLMapper();
            mapper.setFilterProvider(filterProvider);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
            val module = new SimpleModule();
            module.addSerializer(Resource.class, new ResourceSerializer());
            mapper.registerModule(module);
            mapper.writeValue(writer, properties);
            val resource = new ByteArrayResource(writer.toString().getBytes(StandardCharsets.UTF_8));
            return CasCoreConfigurationUtils.loadYamlProperties(resource);
        }
    }

    /**
     * As map .
     *
     * @param withHolder the with holder
     * @return the map
     */
    public static Map<String, Object> asMap(final Serializable withHolder) {
        return asMap(withHolder, new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    /**
     * Gets property name.
     *
     * @param <T>      the type parameter
     * @param <V>      the type parameter
     * @param clazz    the clazz
     * @param supplier the supplier
     * @return the property name
     */
    public static <T, V> String getPropertyName(final Class<T> clazz, final TypedPropertyGetter<T, V> supplier) {
        return PropertyUtils.getPropertyName(clazz, supplier);
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
