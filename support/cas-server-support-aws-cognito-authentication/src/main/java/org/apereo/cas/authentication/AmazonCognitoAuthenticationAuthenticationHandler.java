package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.cognito.AmazonCognitoAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link AmazonCognitoAuthenticationAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AmazonCognitoAuthenticationAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final CognitoIdentityProviderClient cognitoIdentityProvider;

    private final AmazonCognitoAuthenticationProperties properties;

    private final ConfigurableJWTProcessor jwtProcessor;

    public AmazonCognitoAuthenticationAuthenticationHandler(
        final ServicesManager servicesManager,
        final PrincipalFactory principalFactory,
        final CognitoIdentityProviderClient cognitoIdentityProvider,
        final AmazonCognitoAuthenticationProperties properties,
        final ConfigurableJWTProcessor jwtProcessor) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder());
        this.cognitoIdentityProvider = cognitoIdentityProvider;
        this.properties = properties;
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {
        try {
            val authParams = new HashMap<String, String>();
            authParams.put("USERNAME", credential.getUsername());
            authParams.put("PASSWORD", credential.toPassword());
            val authRequest = AdminInitiateAuthRequest.builder();

            val request = authRequest.authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .clientId(properties.getClientId())
                .userPoolId(properties.getUserPoolId())
                .authParameters(authParams).build();
            val result = cognitoIdentityProvider.adminInitiateAuth(request);

            if ("NEW_PASSWORD_REQUIRED".equalsIgnoreCase(result.challengeNameAsString())) {
                throw new CredentialExpiredException();
            }
            val authenticationResult = result.authenticationResult();
            val claims = jwtProcessor.process(authenticationResult.idToken(), new SimpleSecurityContext());
            if (StringUtils.isBlank(claims.getSubject())) {
                throw new FailedLoginException("Unable to accept the ID token with an invalid [sub] claim");
            }

            val userResult = cognitoIdentityProvider.adminGetUser(AdminGetUserRequest.builder()
                .userPoolId(properties.getUserPoolId())
                .username(credential.getUsername()).build());

            val attributes = new LinkedHashMap<String, List<Object>>();
            attributes.put("userStatus", CollectionUtils.wrap(userResult.userStatusAsString()));
            attributes.put("userCreatedDate", CollectionUtils.wrap(userResult.userCreateDate().toEpochMilli()));
            attributes.put("userModifiedDate", CollectionUtils.wrap(userResult.userLastModifiedDate().toEpochMilli()));

            val userAttributes = userResult.userAttributes();
            userAttributes.forEach(attr -> {
                if (!properties.getMappedAttributes().isEmpty() && properties.getMappedAttributes().containsKey(attr.name())) {
                    val newName = properties.getMappedAttributes().get(attr.name());
                    attributes.put(newName, CollectionUtils.wrap(attr.value()));
                } else {
                    attributes.put(attr.name(), CollectionUtils.wrap(attr.value()));
                }
            });

            val principal = principalFactory.createPrincipal(userResult.username(), attributes);
            return createHandlerResult(credential, principal, new ArrayList<>());
        } catch (final NotAuthorizedException e) {
            val message = e.getMessage();
            if (message.contains("expired")) {
                throw new AccountExpiredException(message);
            }
            if (message.contains("disabled")) {
                throw new AccountDisabledException(message);
            }
            throw new FailedLoginException(e.getMessage());
        } catch (final UserNotFoundException e) {
            throw new AccountNotFoundException(e.getMessage());
        } catch (final CredentialExpiredException | InvalidPasswordException e) {
            throw new AccountPasswordMustChangeException(e.getMessage());
        } catch (final Throwable e) {
            throw new FailedLoginException(e.getMessage());
        }
    }
}
