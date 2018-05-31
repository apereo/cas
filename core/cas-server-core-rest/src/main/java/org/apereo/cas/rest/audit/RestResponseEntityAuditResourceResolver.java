package org.apereo.cas.rest.audit;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
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
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        if (returnValue instanceof ResponseEntity) {
            return getAuditResourceFromResponseEntity((ResponseEntity) returnValue);
        }
        return new String[]{};
    }

    private String[] getAuditResourceFromResponseEntity(final ResponseEntity entity) {
        final var headers = entity.getHeaders();
        final var result =
            new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        result.append("status", entity.getStatusCodeValue() + "-" + entity.getStatusCode().name());
        final var location = headers.getLocation();
        if (location != null) {
            result.append("location", location);
        }
        if (this.includeEntityBody && entity.getBody() != null) {
            result.append("body", entity.getBody().toString());
        }
        return new String[]{result.toString()};
    }
}
