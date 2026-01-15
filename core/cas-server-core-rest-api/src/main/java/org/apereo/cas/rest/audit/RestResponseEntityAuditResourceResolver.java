package org.apereo.cas.rest.audit;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    public String[] resolveFrom(final JoinPoint auditableTarget, @Nullable final Object returnValue) {
        if (returnValue instanceof final ResponseEntity entity) {
            return getAuditResourceFromResponseEntity(entity);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private String[] getAuditResourceFromResponseEntity(final ResponseEntity entity) {
        val headers = entity.getHeaders();
        val values = new HashMap<>();
        values.put("status", entity.getStatusCode().value() + "-" + HttpStatus.valueOf(entity.getStatusCode().value()));
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
