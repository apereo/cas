package org.apereo.cas.audit.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.validation.Assertion;
import org.aspectj.lang.JoinPoint;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the ResourceResolver that can determine the Ticket Id from
 * the first parameter of the method call as well as the returned value, typically assertion.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class TicketValidationResourceResolver extends TicketAsFirstParameterResourceResolver {
    
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object object) {
        final List<String> auditResourceResults = new ArrayList<>();

        final String ticketId = AopUtils.unWrapJoinPoint(joinPoint).getArgs()[0].toString();
        auditResourceResults.add(ticketId);

        if (object instanceof Assertion) {
            final Assertion assertion = Assertion.class.cast(object);
            final Authentication authn = assertion.getPrimaryAuthentication();

            try (StringWriter writer = new StringWriter()) {
                final ObjectWriter objectWriter = mapper.writer();

                final Map<String, Object> results = new LinkedHashMap<>();
                results.put("principal", authn.getPrincipal().getId());

                final Map<String, Object> attributes = new LinkedHashMap<>(authn.getAttributes());
                attributes.putAll(authn.getPrincipal().getAttributes());
                results.put("attributes", attributes);

                objectWriter.writeValue(writer, results);
                auditResourceResults.add(writer.toString());
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return auditResourceResults.toArray(new String[]{});
    }
}
