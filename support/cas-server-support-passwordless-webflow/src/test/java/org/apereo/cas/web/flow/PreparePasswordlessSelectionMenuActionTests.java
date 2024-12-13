package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasDelegatedAuthenticationCasAutoConfiguration;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PreparePasswordlessSelectionMenuActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Mail")
@EnabledIfListeningOnPort(port = 25000)
@ImportAutoConfiguration(CasDelegatedAuthenticationCasAutoConfiguration.class)
@Import({
    PreparePasswordlessSelectionMenuActionTests.MultifactorTestConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "cas.authn.pac4j.cas[0].login-url=https://casserver.herokuapp.com/cas/login",
    "cas.authn.pac4j.cas[0].protocol=CAS30",
    "cas.authn.mfa.triggers.global.global-provider-id=" + TestMultifactorAuthenticationProvider.ID
})
class PreparePasswordlessSelectionMenuActionTests extends BasePasswordlessAuthenticationActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_SELECTION_MENU)
    private Action prepareLoginAction;

    @Test
    void verifySelectionMenu() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .email("casuser@example.org")
            .multifactorAuthenticationEligible(TriStateBoolean.TRUE)
            .delegatedAuthenticationEligible(TriStateBoolean.TRUE)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertThrows(IllegalStateException.class, () -> prepareLoginAction.execute(context));

        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account.withAllowSelectionMenu(true));
        assertNull(prepareLoginAction.execute(context));
        assertTrue(PasswordlessWebflowUtils.isMultifactorAuthenticationAllowed(context));
        assertTrue(PasswordlessWebflowUtils.isDelegatedAuthenticationAllowed(context));
        assertTrue(PasswordlessWebflowUtils.isPasswordlessAuthenticationEnabled(context));
    }

    @TestConfiguration(value = "MultifactorTestConfiguration", proxyBeanMethods = false)
    static class MultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

}
