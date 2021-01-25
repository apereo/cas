package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;

/**
 * Converts the Credential object into a String resource identifier.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 */
@RequiredArgsConstructor
public class CredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String SUPPLIED_CREDENTIALS = "Supplied credentials: ";

    private final CasConfigurationProperties casProperties;

    /**
     * Turn the arguments into a list.
     *
     * @param args the args
     * @return the string[]
     */
    private String[] toResources(final Object[] args) {
        val object = args[0];
        if (object instanceof AuthenticationTransaction) {
            val transaction = AuthenticationTransaction.class.cast(object);
            return new String[]{toResourceString(transaction.getCredentials())};
        }
        return new String[]{toResourceString(CollectionUtils.wrap(object))};
    }

    @SneakyThrows
    private String toResourceString(final Object credential) {
        val fmt = casProperties.getAudit().getEngine().getAuditFormat();
        if (fmt == AuditEngineProperties.AuditFormatTypes.JSON) {
            return MAPPER.writeValueAsString(credential);
        }
        return SUPPLIED_CREDENTIALS + credential;
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return toResources(AopUtils.unWrapJoinPoint(joinPoint).getArgs());
    }
}
