package org.apereo.cas.util.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.base.Throwables;
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
public class JasigRegisteredServiceDeserializationProblemHandler extends DeserializationProblemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JasigRegisteredServiceDeserializationProblemHandler.class);

    @Override
    public JavaType handleUnknownTypeId(final DeserializationContext ctxt,
                                        final JavaType baseType,
                                        final String subTypeId, final TypeIdResolver idResolver,
                                        final String failureMsg) throws IOException {

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
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean handleUnknownProperty(final DeserializationContext ctxt, final JsonParser p,
                                         final JsonDeserializer<?> deserializer,
                                         final Object beanOrClass,
                                         final String propertyName) throws IOException {
        boolean handled = false;
        if (beanOrClass instanceof CachingPrincipalAttributesRepository) {
            final CachingPrincipalAttributesRepository repo = CachingPrincipalAttributesRepository.class.cast(beanOrClass);
            switch (propertyName) {
                case "duration":
                    for (int i = 1; i <= 6; i++) {
                        p.nextToken();
                    }
                    final String timeUnit = p.getText();
                    for (int i = 1; i <= 3; i++) {
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

    @Override
    public Object handleWeirdKey(final DeserializationContext ctxt, final Class<?> rawKeyType, final String keyValue, final String failureMsg) throws IOException {
        return super.handleWeirdKey(ctxt, rawKeyType, keyValue, failureMsg);
    }

    @Override
    public Object handleWeirdStringValue(final DeserializationContext ctxt, final Class<?> targetType, final String valueToConvert, final String failureMsg) throws IOException {
        return super.handleWeirdStringValue(ctxt, targetType, valueToConvert, failureMsg);
    }

    @Override
    public Object handleWeirdNumberValue(final DeserializationContext ctxt, final Class<?> targetType, final Number valueToConvert, final String failureMsg) throws IOException {
        return super.handleWeirdNumberValue(ctxt, targetType, valueToConvert, failureMsg);
    }

    @Override
    public Object handleUnexpectedToken(final DeserializationContext ctxt, final Class<?> targetType, final JsonToken t, final JsonParser p, final String failureMsg) throws IOException {
        return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg);
    }

    @Override
    public Object handleInstantiationProblem(final DeserializationContext ctxt, final Class<?> instClass, final Object argument, final Throwable t) throws IOException {
        return super.handleInstantiationProblem(ctxt, instClass, argument, t);
    }

    @Override
    public Object handleMissingInstantiator(final DeserializationContext ctxt, final Class<?> instClass, final JsonParser p, final String msg) throws IOException {
        return super.handleMissingInstantiator(ctxt, instClass, p, msg);
    }
}
