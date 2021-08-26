package org.apereo.cas.rest.audit;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

/**
 * This is {@link RestResponseEntityAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class RestResponseEntityAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    private final boolean includeEntityBody;

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        if (returnValue instanceof ResponseEntity) {
            return getAuditResourceFromResponseEntity((ResponseEntity) returnValue);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private String[] getAuditResourceFromResponseEntity(final ResponseEntity entity) {
        val headers = entity.getHeaders();
        val values = new HashMap<>();
        values.put("status", entity.getStatusCodeValue() + "-" + entity.getStatusCode().name());
        val location = headers.getLocation();
        if (location != null) {
            values.put("location", location);
        }
        if (this.includeEntityBody && entity.getBody() != null) {
            values.put("body", entity.getBody().toString());
        }
        return new String[]{auditFormat.serialize(values)};
    }
}
