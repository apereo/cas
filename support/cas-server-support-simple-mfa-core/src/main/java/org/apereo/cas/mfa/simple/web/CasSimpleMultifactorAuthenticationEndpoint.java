package org.apereo.cas.mfa.simple.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.google.common.base.Splitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is {@link CasSimpleMultifactorAuthenticationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@Endpoint(id = "mfaSimple", defaultAccess = Access.NONE)
public class CasSimpleMultifactorAuthenticationEndpoint extends BaseCasRestActuatorEndpoint {
    public CasSimpleMultifactorAuthenticationEndpoint(final CasConfigurationProperties casProperties,
                                                      final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    /**
     * Generate token and produce response entity.
     *
     * @param service       the service
     * @param authorization the authorization
     * @return the response entity
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate simple multifactor authentication token",
        parameters = {
            @Parameter(name = "credential", required = true, description = "Credential header in base64 encoding to carry the user"),
            @Parameter(name = "service", required = true, description = "The target service url for which the token should be issued")
        })
    public ResponseEntity generateToken(
        @RequestParam
        final String service,
        @RequestHeader("Credential")
        final String authorization) {
        return FunctionUtils.doAndHandle(() -> {
            val credential = extractCredential(authorization);
            val givenService = extractService(service);
            val result = generateToken(credential, givenService);
            return ResponseEntity.ok(result);
        }, e -> ResponseEntity.badRequest().body("Invalid or unauthenticated request")).get();
    }

    protected MultifactorAuthenticationTokenResponse generateToken(final Credential credential, final Service givenService) throws Throwable {
        val authnSupport = applicationContext.getBean(AuthenticationSystemSupport.BEAN_NAME, AuthenticationSystemSupport.class);
        return authnSupport.handleInitialAuthenticationTransaction(givenService, credential)
            .getInitialAuthentication()
            .map(Unchecked.function(authentication -> {
                val token = createAndStoreToken(givenService, authentication);
                return new MultifactorAuthenticationTokenResponse(token.getId(),
                    authentication.getPrincipal().getId(),
                    givenService.getId(),
                    token.getExpirationPolicy().getTimeToLive(token));
            }))
            .orElseThrow(() -> new AuthenticationException("Unable to successfully authenticate the user and/or generate token"));
    }

    protected Credential extractCredential(final String base64Credentials) {
        val basicAuthCredentials = Splitter.on(':').splitToList(EncodingUtils.decodeBase64ToString(base64Credentials));
        return new UsernamePasswordCredential(basicAuthCredentials.getFirst(), basicAuthCredentials.get(1));
    }

    protected CasSimpleMultifactorAuthenticationTicket createAndStoreToken(final Service givenService,
                                                                           final Authentication authentication) throws Throwable {
        val principal = authentication.getPrincipal();
        val mfaService = applicationContext.getBean(CasSimpleMultifactorAuthenticationService.BEAN_NAME, CasSimpleMultifactorAuthenticationService.class);
        val token = mfaService.generate(principal, givenService);
        mfaService.store(token);
        return token;
    }

    protected Service extractService(final String service) {
        val serviceFactory = applicationContext.getBean(WebApplicationService.BEAN_NAME_FACTORY, ServiceFactory.class);
        return serviceFactory.createService(service);
    }

    @SuppressWarnings("UnusedVariable")
    private record MultifactorAuthenticationTokenResponse(String id, String principal, String service, long ttl) {
    }
}
