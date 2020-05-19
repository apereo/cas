package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.util.AopUtils;
import org.apereo.cas.validation.Assertion;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
        val auditResourceResults = new ArrayList<String>(2);

        val args = AopUtils.unWrapJoinPoint(joinPoint).getArgs();
        if (args != null && args.length > 0) {
            val ticketId = args[0].toString();
            auditResourceResults.add(ticketId);
        }

        if (object instanceof Assertion) {
            val assertion = Assertion.class.cast(object);
            val authn = assertion.getPrimaryAuthentication();

            try (val writer = new StringWriter()) {
                val objectWriter = mapper.writer();

                val results = new LinkedHashMap<String, Object>();
                results.put("principal", authn.getPrincipal().getId());

                val attributes = new HashMap<String, Object>(authn.getAttributes());
                attributes.putAll(authn.getPrincipal().getAttributes());
                results.put("attributes", attributes);

                objectWriter.writeValue(writer, results);
                auditResourceResults.add(writer.toString());
            } catch (final Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return auditResourceResults.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
