package org.apereo.cas.authentication;

import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.AmazonCognitoAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonCognitoAuthenticationAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    AmazonCognitoAuthenticationConfiguration.class
}, properties = {
    "cas.authn.cognito.userPoolId=us-west-2_igeBNHRsb",
    "cas.authn.cognito.region=us-west-2",
    "cas.authn.cognito.clientExecutionTimeout=30000",
    "cas.authn.cognito.credentialAccessKey=test",
    "cas.authn.cognito.credentialSecretKey=test",
    "cas.authn.cognito.clientId=4o5qr8egumc72iv6qibm8foeh6"
})
@Tag("AmazonWebServices")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AmazonCognitoAuthenticationAuthenticationHandlerTests {
    @Autowired
    @Qualifier("amazonCognitoAuthenticationHandler")
    private AuthenticationHandler amazonCognitoAuthenticationHandler;

    @Autowired
    private CasConfigurationProperties casProperties;

    private static ConfigurableJWTProcessor getConfigurableJWTProcessor(final String sub) throws Exception {
        val jwtProcessor = mock(ConfigurableJWTProcessor.class);
        val claims = new JWTClaimsSet.Builder().subject(sub).build();
        when(jwtProcessor.process(anyString(), any())).thenReturn(claims);
        return jwtProcessor;
    }

    @Test
    public void verifyHandler() {
        assertNotNull(amazonCognitoAuthenticationHandler);
    }

    @Test
    public void verifyExpiredPassword() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");

        val provider = mock(AWSCognitoIdentityProvider.class);
        val initResult1 = new AdminInitiateAuthResult();
        initResult1.setChallengeName("NEW_PASSWORD_REQUIRED");
        when(provider.adminInitiateAuth(any())).thenReturn(initResult1);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-exp-password", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);

        assertThrows(AccountPasswordMustChangeException.class, () -> handler.authenticate(creds));
    }

    @Test
    public void verifyAccountDisabled() throws Exception {
        verifyAccountStatusFailure(new NotAuthorizedException("disabled"), AccountDisabledException.class);
    }

    @Test
    public void verifyAccountExpired() throws Exception {
        verifyAccountStatusFailure(new NotAuthorizedException("expired"), AccountExpiredException.class);
    }

    @Test
    public void verifyAccountFail() throws Exception {
        verifyAccountStatusFailure(new UserNotFoundException("not-found"), AccountNotFoundException.class);
        verifyAccountStatusFailure(new AuthenticationException("not-found"), FailedLoginException.class);
    }

    @Test
    public void verifyAccountNotFound() throws Exception {
        verifyAccountStatusFailure(new NotAuthorizedException("fail"), FailedLoginException.class);
    }

    @Test
    public void verifyAccountPassword() throws Exception {
        verifyAccountStatusFailure(new InvalidPasswordException("fail"), AccountPasswordMustChangeException.class);
    }

    private void verifyAccountStatusFailure(final Exception ex, final Class<? extends Throwable> expected) throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");
        val provider = mock(AWSCognitoIdentityProvider.class);

        when(provider.adminInitiateAuth(any())).thenThrow(ex);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-exp-password", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        assertThrows(expected, () -> handler.authenticate(creds));
    }

    @Test
    public void verifyNoSub() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor(StringUtils.EMPTY);
        val provider = mock(AWSCognitoIdentityProvider.class);
        val initResult1 = new AdminInitiateAuthResult();
        initResult1.setChallengeName("OK");

        val result2 = new AdminInitiateAuthResult();
        val authResult = new AuthenticationResultType();
        authResult.setIdToken("some-id-token");
        result2.setAuthenticationResult(authResult);
        when(provider.adminInitiateAuth(any())).thenReturn(result2);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-ok", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(creds));
    }

    @Test
    public void verifyOK() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");

        val provider = mock(AWSCognitoIdentityProvider.class);
        val initResult1 = new AdminInitiateAuthResult();
        initResult1.setChallengeName("OK");

        val result2 = new AdminInitiateAuthResult();
        val authResult = new AuthenticationResultType();
        authResult.setIdToken("some-id-token");
        result2.setAuthenticationResult(authResult);
        when(provider.adminInitiateAuth(any())).thenReturn(result2);

        val userResult1 = new AdminGetUserResult();
        userResult1.setUserStatus("OK");
        userResult1.setUserCreateDate(new Date());
        userResult1.setUserLastModifiedDate(new Date());
        val type = new AttributeType();
        type.setName("cn");
        type.setName("CAS");
        userResult1.setUserAttributes(List.of(type));
        userResult1.setUsername("casuser");
        when(provider.adminGetUser(argThat(argument -> argument.getUsername().equals("casuser-ok"))))
            .thenReturn(userResult1);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-ok", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        val result = handler.authenticate(creds);
        assertNotNull(result);
    }
}
