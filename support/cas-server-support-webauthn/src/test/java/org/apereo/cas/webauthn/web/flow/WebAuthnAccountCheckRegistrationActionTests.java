package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.data.CredentialRegistration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnAccountCheckRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
class WebAuthnAccountCheckRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEBAUTHN_CHECK_ACCOUNT_REGISTRATION)
    private Action webAuthnCheckAccountRegistrationAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);

        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authentication, context);
        var result = webAuthnCheckAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER, result.getId());

        webAuthnCredentialRepository.addRegistrationByUsername(
            authentication.getPrincipal().getId(), CredentialRegistration.builder().build());
        result = webAuthnCheckAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

}
