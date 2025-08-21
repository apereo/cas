package org.apereo.cas.authentication;

import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasAmazonCognitoAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasAmazonCognitoAuthenticationAutoConfiguration.class
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
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class AmazonCognitoAuthenticationAuthenticationHandlerTests {
    @Autowired
    @Qualifier("amazonCognitoAuthenticationHandler")
    private AuthenticationHandler amazonCognitoAuthenticationHandler;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyHandler() {
        assertNotNull(amazonCognitoAuthenticationHandler);
    }

    @Test
    void verifyExpiredPassword() throws Throwable {
        val jwtProcessor = getConfigurableJWTProcessor("casuser");

        val provider = mock(CognitoIdentityProviderClient.class);
        val initResult1 = AdminInitiateAuthResponse.builder().challengeName("NEW_PASSWORD_REQUIRED").build();
        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(initResult1);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-exp-password", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);

        assertThrows(AccountPasswordMustChangeException.class, () -> handler.authenticate(creds, mock(Service.class)));
    }

    @Test
    void verifyAccountDisabled() throws Throwable {
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("disabled").build(), AccountDisabledException.class);
    }

    @Test
    void verifyAccountExpired() throws Throwable {
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("expired").build(), AccountExpiredException.class);
    }

    @Test
    void verifyAccountFail() throws Throwable {
        verifyAccountStatusFailure(UserNotFoundException.builder().message("no-found").build(), AccountNotFoundException.class);
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("not-found").build(), FailedLoginException.class);
    }

    @Test
    void verifyAccountNotFound() throws Throwable {
        verifyAccountStatusFailure(NotAuthorizedException.builder().message("fail").build(), FailedLoginException.class);
    }

    @Test
    void verifyAccountPassword() throws Throwable {
        verifyAccountStatusFailure(InvalidPasswordException.builder().message("fail").build(), AccountPasswordMustChangeException.class);
    }

    @Test
    void verifyNoSub() throws Throwable {
        val jwtProcessor = getConfigurableJWTProcessor(StringUtils.EMPTY);
        val provider = mock(CognitoIdentityProviderClient.class);

        val authResult = AuthenticationResultType.builder().idToken("some-id-token").build();
        val result2 = AdminInitiateAuthResponse.builder().authenticationResult(authResult).build();
        when(provider.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(result2);

        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-ok", "Hell063!!");
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        assertThrows(FailedLoginException.class, () -> handler.authenticate(creds, mock(Service.class)));
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    void verifyOK() throws Throwable {
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
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        val result = handler.authenticate(creds, mock(Service.class));
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    void verifyOKWithMappedAttributes() throws Throwable {
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

        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(
            PrincipalFactoryUtils.newPrincipalFactory(), provider,
            casProperties.getAuthn().getCognito(), jwtProcessor);
        val result = handler.authenticate(creds, mock(Service.class));
        assertEquals("cas789", result.getPrincipal().getAttributes().get("netid").getFirst());
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
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(
            PrincipalFactoryUtils.newPrincipalFactory(), provider, casProperties.getAuthn().getCognito(), jwtProcessor);
        assertThrows(expected, () -> handler.authenticate(creds, mock(Service.class)));
    }
}
