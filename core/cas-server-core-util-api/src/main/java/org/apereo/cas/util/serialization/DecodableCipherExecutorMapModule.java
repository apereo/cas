package org.apereo.cas.util.serialization;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.MapType;

/**
 * This is {@link DecodableCipherExecutorMapModule}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DecodableCipherExecutorMapModule extends SimpleModule {
    @Serial
    private static final long serialVersionUID = 8418506749514188867L;

    private final ApplicationContext applicationContext;


    @Override
    public void setupModule(final SetupContext setupContext) {
        val cipherExecutor = ApplicationContextProvider.getBean(applicationContext,
                CipherExecutor.BEAN_NAME_CAS_CONFIGURATION_CIPHER_EXECUTOR, CipherExecutor.class)
            .orElseGet(CipherExecutor::noOp);

        setupContext.addDeserializerModifier(new ValueDeserializerModifier() {
            @Serial
            private static final long serialVersionUID = 2351328333366612397L;

            @Override
            public ValueDeserializer<?> modifyMapDeserializer(final DeserializationConfig config,
                                                              final MapType type,
                                                              final BeanDescription.Supplier beanDescRef,
                                                              final ValueDeserializer jsonDeserializer) {
                return new DecryptingMapDeserializer(jsonDeserializer, cipherExecutor);
            }
        });
    }

    @RequiredArgsConstructor
    private static final class DecryptingMapDeserializer extends ValueDeserializer<Map<?, Object>> {
        private final ValueDeserializer<Map<?, ?>> defaultDeserializer;
        private final CipherExecutor cipherExecutor;

        @Override
        public void resolve(final DeserializationContext deserializationContext) throws JacksonException {
            defaultDeserializer.resolve(deserializationContext);
        }


        @Override
        public ValueDeserializer<?> createContextual(final DeserializationContext deserializationContext,
                                                     final BeanProperty property) {
            if (property == null || property.getAnnotation(DecodableCipherMap.class) == null) {
                return defaultDeserializer;
            }
            return new DecryptingMapDeserializer(defaultDeserializer, cipherExecutor);
        }

        @Override
        public Map<String, Object> deserialize(final JsonParser jsonParser,
                                               final DeserializationContext deserializationContext) throws JacksonException {
            val properties = defaultDeserializer.deserialize(jsonParser, deserializationContext);
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val effectiveMap = properties.entrySet()
                .stream()
                .collect(Collectors.toMap(
                    entry -> resolver.resolve(entry.getKey().toString()),
                    entry -> resolver.resolve(entry.getValue().toString())));
            return cipherExecutor.decode(effectiveMap, ArrayUtils.EMPTY_OBJECT_ARRAY);
        }
    }
}
