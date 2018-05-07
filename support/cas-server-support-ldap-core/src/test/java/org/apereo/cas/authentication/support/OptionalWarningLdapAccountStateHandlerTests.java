package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OptionalWarningLdapAccountStateHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
public class OptionalWarningLdapAccountStateHandlerTests {

    @Test
    public void verifyWarningOnMatch() {
        final var h = new OptionalWarningLdapAccountStateHandler();
        h.setWarnAttributeName("attribute");
        h.setWarningAttributeValue("value");
        h.setDisplayWarningOnMatch(true);
        final var response = mock(AuthenticationResponse.class);
        final var entry = mock(LdapEntry.class);
        when(response.getLdapEntry()).thenReturn(entry);
        when(entry.getAttribute(anyString())).thenReturn(new LdapAttribute("attribute", "value"));
        final List<MessageDescriptor> messages = new ArrayList<>();
        final var config = new PasswordPolicyConfiguration();
        config.setPasswordWarningNumberOfDays(5);
        h.handleWarning(new AccountState.DefaultWarning(ZonedDateTime.now(), 1),
            response, config, messages);
        assertEquals(2, messages.size());
    }

    @Test
    public void verifyAlwaysWarningOnMatch() {
        final var h = new OptionalWarningLdapAccountStateHandler();
        h.setWarnAttributeName("attribute");
        h.setWarningAttributeValue("value");
        h.setDisplayWarningOnMatch(true);
        final var response = mock(AuthenticationResponse.class);
        final var entry = mock(LdapEntry.class);
        when(response.getLdapEntry()).thenReturn(entry);
        when(entry.getAttribute(anyString())).thenReturn(new LdapAttribute("attribute", "value"));
        final List<MessageDescriptor> messages = new ArrayList<>();
        final var config = new PasswordPolicyConfiguration();
        config.setAlwaysDisplayPasswordExpirationWarning(true);
        h.handleWarning(new AccountState.DefaultWarning(ZonedDateTime.now(), 1),
            response, config, messages);
        assertEquals(2, messages.size());
    }

    @Test
    public void verifyNoWarningOnMatch() {
        final var h = new OptionalWarningLdapAccountStateHandler();
        h.setWarnAttributeName("attribute");
        h.setWarningAttributeValue("value");
        h.setDisplayWarningOnMatch(false);
        final var response = mock(AuthenticationResponse.class);
        final var entry = mock(LdapEntry.class);
        when(response.getLdapEntry()).thenReturn(entry);
        when(entry.getAttribute(anyString())).thenReturn(new LdapAttribute("attribute", "value"));
        final List<MessageDescriptor> messages = new ArrayList<>();
        final var config = new PasswordPolicyConfiguration();
        config.setPasswordWarningNumberOfDays(5);
        h.handleWarning(new AccountState.DefaultWarning(ZonedDateTime.now(), 1),
            response, config, messages);
        assertEquals(0, messages.size());
    }
}
