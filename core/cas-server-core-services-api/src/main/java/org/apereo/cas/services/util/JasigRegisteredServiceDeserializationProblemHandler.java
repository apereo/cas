package org.apereo.cas.services.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.apache.commons.lang3.ClassUtils;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This is {@link JasigRegisteredServiceDeserializationProblemHandler}
 * that attempts load JSON definitions assigned to the `org.jasig`
 * namespace. This component should be registered globally with JSON object mappers.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
class JasigRegisteredServiceDeserializationProblemHandler extends DeserializationProblemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JasigRegisteredServiceDeserializationProblemHandler.class);
    private static final int TOKEN_COUNT_DURATION = 6;
    private static final int TOKEN_COUNT_EXPIRATION = 3;

    @Override
    public JavaType handleUnknownTypeId(final DeserializationContext ctxt,
                                        final JavaType baseType,
                                        final String subTypeId, final TypeIdResolver idResolver,
                                        final String failureMsg) {

        try {
            if (subTypeId.contains("org.jasig.")) {
                final String newTypeName = subTypeId.replaceAll("jasig", "apereo");
                LOGGER.warn("Found legacy CAS JSON definition type identified as [{}]. "
                                + "While CAS will attempt to convert the legacy definition to [{}] for the time being, "
                                + "the definition SHOULD manually be upgraded to the new supported syntax",
                        subTypeId, newTypeName);
                final Class newType = ClassUtils.getClass(newTypeName);
                return SimpleType.construct(newType);
            }
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean handleUnknownProperty(final DeserializationContext ctxt, final JsonParser p,
                                         final JsonDeserializer<?> deserializer,
                                         final Object beanOrClass, final String propertyName) throws IOException {
        boolean handled = false;
        if (beanOrClass instanceof CachingPrincipalAttributesRepository) {
            final CachingPrincipalAttributesRepository repo = CachingPrincipalAttributesRepository.class.cast(beanOrClass);
            switch (propertyName) {
                case "duration":
                    for (int i = 1; i <= TOKEN_COUNT_DURATION; i++) {
                        p.nextToken();
                    }
                    final String timeUnit = p.getText();
                    for (int i = 1; i <= TOKEN_COUNT_EXPIRATION; i++) {
                        p.nextToken();
                    }
                    final int expiration = p.getValueAsInt();

                    repo.setTimeUnit(timeUnit);
                    repo.setExpiration(expiration);

                    LOGGER.warn("CAS has converted legacy JSON property [{}] for type [{}]. It parsed 'expiration' value [{}] with time unit of [{}]."
                                    + "It is STRONGLY recommended that you review the configuration and upgrade from the legacy syntax.",
                            propertyName, beanOrClass.getClass().getName(), expiration, timeUnit);

                    handled = true;
                    break;
                default:
                    break;
            }
        }

        return handled;
    }
}
