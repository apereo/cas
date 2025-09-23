package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationAction;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is {@link GoogleAuthenticatorAccountCreateRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GoogleAuthenticatorValidateTokenActionTests.TestMultifactorTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
}, properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
class GoogleAuthenticatorAccountCreateRegistrationActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_CREATE_REGISTRATION)
    private Action googleAccountCreateRegistrationAction;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @ParameterizedTest
    @MethodSource("oneTimeTokenAccountProvider")
    void verifyCreateAccount(final OneTimeTokenAccount account) throws Throwable {
        googleAuthenticatorAccountRegistry.save(account);

        val context = MockRequestContext.create(applicationContext);
        if (StringUtils.isNotBlank(account.getTenant())) {
            context.setContextPath("/tenants/%s/login".formatted(account.getTenant()));
        }
        
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(account.getUsername()), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER, googleAccountCreateRegistrationAction.execute(context).getId());
        assertTrue(context.getFlowScope().contains(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT));
        assertTrue(context.getFlowScope().contains(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_QR_IMAGE_BASE64));
    }

    static Stream<Arguments> oneTimeTokenAccountProvider() {
        return Stream.of(
            arguments(OneTimeTokenAccount.builder()
                .username(UUID.randomUUID().toString())
                .secretKey(UUID.randomUUID().toString())
                .validationCode(123456)
                .scratchCodes(List.of())
                .name(UUID.randomUUID().toString())
                .build()),
            arguments(OneTimeTokenAccount.builder()
                .username(UUID.randomUUID().toString())
                .secretKey(UUID.randomUUID().toString())
                .validationCode(123456)
                .scratchCodes(List.of())
                .name(UUID.randomUUID().toString())
                .tenant("shire")
                .build())
        );
    }
}
