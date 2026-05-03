package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * This is {@link SurrogateEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Endpoint(id = "impersonation", defaultAccess = Access.NONE)
@Slf4j
public class SurrogateEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService;

    public SurrogateEndpoint(final CasConfigurationProperties casProperties,
                             final ConfigurableApplicationContext applicationContext,
                             final ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService) {
        super(casProperties, applicationContext);
        this.surrogateAuthenticationService = surrogateAuthenticationService;
    }

    /**
     * Gets surrogate accounts.
     *
     * @param username the username
     * @return the surrogate accounts
     * @throws Throwable the throwable
     */
    @GetMapping(
        path = "/{username}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        })
    @Operation(summary = "Get surrogate accounts for a given username",
        parameters = @Parameter(name = "username", description = "The username"))
    public Collection<String> getSurrogateAccounts(@PathVariable final String username) throws Throwable {
        val service = surrogateAuthenticationService.getObject();
        return service.getImpersonationAccounts(username, Optional.empty());
    }
}
