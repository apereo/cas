package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.cognito.AmazonCognitoAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

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
public class AmazonCognitoAuthenticationAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements DisposableBean {
    private final AWSCognitoIdentityProvider cognitoIdentityProvider;

    private final AmazonCognitoAuthenticationProperties properties;

    private final ConfigurableJWTProcessor jwtProcessor;

    public AmazonCognitoAuthenticationAuthenticationHandler(final String name,
                                                            final ServicesManager servicesManager,
                                                            final PrincipalFactory principalFactory,
                                                            final AWSCognitoIdentityProvider cognitoIdentityProvider,
                                                            final AmazonCognitoAuthenticationProperties properties,
                                                            final ConfigurableJWTProcessor jwtProcessor) {
        super(name, servicesManager, principalFactory, properties.getOrder());
        this.cognitoIdentityProvider = cognitoIdentityProvider;
        this.properties = properties;
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    public void destroy() {
        this.cognitoIdentityProvider.shutdown();
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {
        try {
            val authParams = new HashMap<String, String>();
            authParams.put("USERNAME", credential.getUsername());
            authParams.put("PASSWORD", credential.getPassword());
            val authRequest = new AdminInitiateAuthRequest();

            authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withClientId(properties.getClientId())
                .withUserPoolId(properties.getUserPoolId())
                .withAuthParameters(authParams);

            val result = cognitoIdentityProvider.adminInitiateAuth(authRequest);

            if ("NEW_PASSWORD_REQUIRED".equalsIgnoreCase(result.getChallengeName())) {
                throw new CredentialExpiredException();
            }
            val authenticationResult = result.getAuthenticationResult();
            val claims = jwtProcessor.process(authenticationResult.getIdToken(), new SimpleSecurityContext());
            if (StringUtils.isBlank(claims.getSubject())) {
                throw new FailedLoginException("Unable to accept the id token with an invalid [sub] claim");
            }

            val userRequest = new AdminGetUserRequest()
                .withUsername(credential.getUsername())
                .withUserPoolId(properties.getUserPoolId());

            val userResult = cognitoIdentityProvider.adminGetUser(userRequest);
            val attributes = new LinkedHashMap<String, List<Object>>();
            attributes.put("userStatus", CollectionUtils.wrap(userResult.getUserStatus()));
            attributes.put("userCreatedDate", CollectionUtils.wrap(userResult.getUserCreateDate()));
            attributes.put("userModifiedDate", CollectionUtils.wrap(userResult.getUserLastModifiedDate()));

            val userAttributes = userResult.getUserAttributes();
            userAttributes.forEach(attr -> attributes.put(attr.getName(), CollectionUtils.wrap(attr.getValue())));

            val principal = principalFactory.createPrincipal(userResult.getUsername(), attributes);
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        } catch (final NotAuthorizedException e) {
            val message = e.getMessage();
            if (message.contains("expired")) {
                throw new AccountExpiredException(message);
            }
            if (message.contains("disabled")) {
                throw new AccountDisabledException(message);
            }
            throw new FailedLoginException(e.getErrorMessage());
        } catch (final UserNotFoundException e) {
            throw new AccountNotFoundException(e.getMessage());
        } catch (final InvalidPasswordException e) {
            throw new AccountPasswordMustChangeException(e.getMessage());
        } catch (final CredentialExpiredException e) {
            throw new AccountPasswordMustChangeException(e.getMessage());
        } catch (final Exception e) {
            throw new FailedLoginException(e.getMessage());
        }
    }
}
