package org.apereo.cas.authentication;

import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.AmazonCognitoAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

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
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.time.Clock;
import java.time.Instant;
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
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    AmazonCognitoAuthenticationConfiguration.class
}, properties = {
    "cas.authn.cognito.user-pool-id=us-west-2_igeBNHRsb",
    "cas.authn.cognito.region=us-west-2",
    "cas.authn.cognito.client-execution-timeout=30000",
    "cas.authn.cognito.credential-access-key=test",
    "cas.authn.cognito.credential-secret-key=test",
    "cas.authn.cognito.client-id=4o5qr8egumc72iv6qibm8foeh6",
    "cas.authn.cognito.mapped-attributes.[custom\\:netid]=netid"
})
@Tag("AmazonWebServices")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AmazonCognitoAuthenticationAuthenticationHandlerTests {
    @Autowired
    @Qualifier("amazonCognitoAuthenticationHandler")
    private AuthenticationHandler amazonCognitoAuthenticationHandler;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyHandler() {
        assertNotNull(amazonCognitoAuthenticationHandler);
    }

    @Test
    public void verifyExpiredPassword() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");

        val provider = mock(CognitoIdentityProviderClient.class);
        val initResult1 = AdminInitiateAuthResponse.builder().challengeName("NEW_PASSWORD_REQUIRED").build();
        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(initResult1);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-exp-password", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);

        assertThrows(AccountPasswordMustChangeException.class, () -> handler.authenticate(creds));
    }

    @Test
    public void verifyAccountDisabled() throws Exception {
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("disabled").build(), AccountDisabledException.class);
    }

    @Test
    public void verifyAccountExpired() throws Exception {
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("expired").build(), AccountExpiredException.class);
    }

    @Test
    public void verifyAccountFail() throws Exception {
        verifyAccountStatusFailure(UserNotFoundException.builder().message("no-found").build(), AccountNotFoundException.class);
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("not-found").build(), FailedLoginException.class);
    }

    @Test
    public void verifyAccountNotFound() throws Exception {
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("fail").build(), FailedLoginException.class);
    }

    @Test
    public void verifyAccountPassword() throws Exception {
        verifyAccountStatusFailure(InvalidPasswordException.builder().message("fail").build(), AccountPasswordMustChangeException.class);
    }

    @Test
    public void verifyNoSub() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor(StringUtils.EMPTY);
        val provider = mock(CognitoIdentityProviderClient.class);

        val authResult = AuthenticationResultType.builder().idToken("some-id-token").build();
        val result2 = AdminInitiateAuthResponse.builder().authenticationResult(authResult).build();
        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(result2);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-ok", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(creds));
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    public void verifyOK() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");
        val provider = mock(CognitoIdentityProviderClient.class);

        val authResult = AuthenticationResultType.builder().idToken("some-id-token").build();
        val result2 = AdminInitiateAuthResponse.builder().authenticationResult(authResult).build();
        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(result2);

        val userResult1 = AdminGetUserResponse.builder()
            .username("casuser")
            .userStatus("OK")
            .userCreateDate(Instant.now(Clock.systemUTC()))
            .userLastModifiedDate(Instant.now(Clock.systemUTC()))
            .userAttributes(List.of(AttributeType.builder().name("CAS").build()))
            .build();

        when(provider.adminGetUser(any(AdminGetUserRequest.class))).thenReturn(userResult1);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-ok", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        val result = handler.authenticate(creds);
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    public void verifyOKWithMappedAttributes() throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");
        val provider = mock(CognitoIdentityProviderClient.class);

        val authResult = AuthenticationResultType.builder().idToken("some-id-token").build();
        val result2 = AdminInitiateAuthResponse.builder().authenticationResult(authResult).build();
        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(result2);

        val userResult1 = AdminGetUserResponse.builder()
                .username("casuser")
                .userStatus("OK")
                .userCreateDate(Instant.now(Clock.systemUTC()))
                .userLastModifiedDate(Instant.now(Clock.systemUTC()))
                .userAttributes(List.of(AttributeType.builder().name("custom:netid").value("cas789").build()))
                .build();

        when(provider.adminGetUser(any(AdminGetUserRequest.class))).thenReturn(userResult1);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-ok", "Hell063!!");

        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
                PrincipalFactoryUtils.newPrincipalFactory(), provider,
                casProperties.getAuthn().getCognito(), jwtProcessor);
        val result = handler.authenticate(creds);
        assertEquals("cas789", result.getPrincipal().getAttributes().get("netid").get(0));
    }

    private static ConfigurableJWTProcessor getConfigurableJWTProcessor(final String sub) throws Exception {
        val jwtProcessor = mock(ConfigurableJWTProcessor.class);
        val claims = new JWTClaimsSet.Builder().subject(sub).build();
        when(jwtProcessor.process(anyString(), any())).thenReturn(claims);
        return jwtProcessor;
    }

    private void verifyAccountStatusFailure(final Exception ex, final Class<? extends Throwable> expected) throws Exception {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");
        val provider = mock(CognitoIdentityProviderClient.class);

        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenThrow(ex);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-exp-password", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(getClass().getSimpleName(), mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        assertThrows(expected, () -> handler.authenticate(creds));
    }
}
