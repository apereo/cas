package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.validation.Assertion;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.spi.support.ParametersAsStringResourceResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This is {@link ProtocolSpecificationValidationAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class ProtocolSpecificationValidationAuditResourceResolver extends ParametersAsStringResourceResolver {
    private final CasConfigurationProperties casProperties;

    @Override
    protected String[] createResource(final Object[] args) {
        val results = new LinkedHashMap<>();
        Arrays.stream(args).forEach(arg -> {
            if (arg instanceof HttpServletRequest) {
                val request = HttpServletRequest.class.cast(arg);
                results.put(CasProtocolConstants.PARAMETER_RENEW,
                    StringUtils.defaultString(request.getParameter(CasProtocolConstants.PARAMETER_RENEW), "false"));
                results.put(CasProtocolConstants.PARAMETER_GATEWAY,
                    StringUtils.defaultString(request.getParameter(CasProtocolConstants.PARAMETER_GATEWAY), "false"));
            }
            if (arg instanceof Assertion) {
                val assertion = Assertion.class.cast(arg);
                val authn = assertion.getPrimaryAuthentication();
                results.put("principal", authn.getPrincipal().getId());
                results.put("service", DigestUtils.abbreviate(assertion.getService().getId()));
                if (casProperties.getAudit().getEngine().isIncludeValidationAssertion()) {
                    val attributes = new HashMap<String, Object>(authn.getAttributes());
                    attributes.putAll(authn.getPrincipal().getAttributes());
                    results.put("attributes", attributes);
                }
            }
        });
        return results.isEmpty()
            ? ArrayUtils.EMPTY_STRING_ARRAY
            : new String[]{auditFormat.serialize(results)};
    }
}
