package org.apereo.cas.util.serialization;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;

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
    public void setupModule(final Module.SetupContext setupContext) {
        val cipherExecutor = ApplicationContextProvider.getBean(applicationContext,
            CipherExecutor.BEAN_NAME_CAS_CONFIGURATION_CIPHER_EXECUTOR, CipherExecutor.class)
            .orElseGet(CipherExecutor::noOp);

        setupContext.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Serial
            private static final long serialVersionUID = 2351328333366612397L;

            @Override
            public JsonDeserializer<?> modifyMapDeserializer(final DeserializationConfig config,
                                                             final MapType type,
                                                             final BeanDescription beanDescription,
                                                             final JsonDeserializer jsonDeserializer) {
                return new DecryptingMapDeserializer(jsonDeserializer, cipherExecutor);
            }
        });
    }

    @RequiredArgsConstructor
    private static final class DecryptingMapDeserializer extends JsonDeserializer<Map<String, Object>>
        implements ContextualDeserializer, ResolvableDeserializer {
        private final JsonDeserializer<Map<String, ?>> delegate;
        private final CipherExecutor cipherExecutor;

        @Override
        public void resolve(final DeserializationContext deserializationContext) throws JsonMappingException {
            if (delegate instanceof final ResolvableDeserializer deserializer) {
                deserializer.resolve(deserializationContext);
            }
        }

        @Override
        public JsonDeserializer<?> createContextual(final DeserializationContext deserializationContext,
                                                    final BeanProperty property) throws JsonMappingException {
            val jsonDeserializer = delegate instanceof final ContextualDeserializer deserializer
                ? deserializer.createContextual(deserializationContext, property)
                : delegate;
            return new DecryptingMapDeserializer((JsonDeserializer<Map<String, ?>>) jsonDeserializer, cipherExecutor);
        }

        @Override
        public Map<String, Object> deserialize(final JsonParser jsonParser,
                                               final DeserializationContext deserializationContext) throws IOException {
            val properties = (Map) delegate.deserialize(jsonParser, deserializationContext);
            return cipherExecutor.decode(properties, ArrayUtils.EMPTY_OBJECT_ARRAY);
        }
    }
}
