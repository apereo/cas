package org.apereo.cas.web.report;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasResolveAttributesReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Endpoint(id = "resolveAttributes", defaultAccess = Access.NONE)
public class CasResolveAttributesReportEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    public CasResolveAttributesReportEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<PrincipalResolver> defaultPrincipalResolver) {
        super(casProperties);
        this.defaultPrincipalResolver = defaultPrincipalResolver;
    }
    
    /**
     * Resolve principal attributes map.
     *
     * @param username the uid
     * @return the map
     * @throws Throwable the throwable
     */
    @ReadOperation
    @Operation(summary = "Resolve principal attributes for user",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to resolve attributes for"))
    public Map<String, Object> resolvePrincipalAttributes(@Selector final String username) throws Throwable {
        val map = new HashMap<String, Object>();
        val principal = defaultPrincipalResolver.getObject().resolve(new BasicIdentifiableCredential(username));
        if (!(principal instanceof NullPrincipal)) {
            map.put("username", principal.getId());
            map.put("attributes", principal.getAttributes());
        }
        return map;
    }
}
