package org.apereo.cas.authentication;

import org.apereo.cas.config.AmazonCognitoAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

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
})
@TestPropertySource(properties = {
    "cas.authn.cognito.userPoolId=us-west-2_igeBNHRsb",
    "cas.authn.cognito.region=us-west-2",
    "cas.authn.cognito.clientExecutionTimeout=30000",
    "cas.authn.cognito.credentialAccessKey=test",
    "cas.authn.cognito.credentialSecretKey=test",
    "cas.authn.cognito.clientId=4o5qr8egumc72iv6qibm8foeh6"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@IfProfileValue(name = "cognitoEnabled", value = "true")
public class AmazonCognitoAuthenticationAuthenticationHandlerTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("amazonCognitoAuthenticationHandler")
    private AuthenticationHandler amazonCognitoAuthenticationHandler;

    @Test
    public void verifyAction() throws Exception {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Hell063!!");
        amazonCognitoAuthenticationHandler.authenticate(creds);
    }
}
