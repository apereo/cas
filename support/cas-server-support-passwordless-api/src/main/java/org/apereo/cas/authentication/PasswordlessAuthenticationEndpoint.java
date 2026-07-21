package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is {@link PasswordlessAuthenticationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Endpoint(id = "passwordless", defaultAccess = Access.NONE)
public class PasswordlessAuthenticationEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<PasswordlessUserAccountStore> passwordlessUserAccountStoreProvider;
    private final ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    public PasswordlessAuthenticationEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<PasswordlessUserAccountStore> passwordlessUserAccountStoreProvider,
        final ObjectProvider<PrincipalResolver> defaultPrincipalResolver) {
        super(casProperties, applicationContext);
        this.passwordlessUserAccountStoreProvider = passwordlessUserAccountStoreProvider;
        this.defaultPrincipalResolver = defaultPrincipalResolver;
    }

    /**
     * Gets account.
     *
     * @param username the username
     * @return the account
     * @throws Throwable the throwable
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get passwordless account by username",
        description = "Retrieves the passwordless user account associated with the specified username.",
        parameters = @Parameter(
            name = "username",
            description = "Username associated with the passwordless account",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string"),
            example = "casuser"
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Passwordless account was found",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PasswordlessUserAccount.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "No passwordless account was found for the specified username"
            )
        }
    )
    public ResponseEntity getAccount(final String username) throws Throwable {
        if (StringUtils.isBlank(username)) {
            return ResponseEntity.notFound().build();
        }
        val request = PasswordlessAuthenticationRequest.builder()
            .username(username)
            .build();
        val result = passwordlessUserAccountStoreProvider.getObject().findUser(request);
        return result
            .map(Unchecked.function(account -> {
                val principal = defaultPrincipalResolver.getObject().resolve(new BasicIdentifiableCredential(username));
                return (principal instanceof NullPrincipal)
                    ? account
                    : account.withAttributes(CoreAuthenticationUtils.mergeAttributes(
                        (Map) account.getAttributes(), Objects.requireNonNull(principal).getAttributes()));
            }))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
