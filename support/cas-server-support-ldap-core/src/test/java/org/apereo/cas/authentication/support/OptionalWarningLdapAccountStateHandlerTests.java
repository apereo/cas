package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OptionalWarningLdapAccountStateHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
public class OptionalWarningLdapAccountStateHandlerTests {
    @Test
    public void verifyWarningOnMatch() {
        val h = new OptionalWarningLdapAccountStateHandler();
        h.setWarnAttributeName("attribute");
        h.setWarningAttributeValue("value");
        h.setDisplayWarningOnMatch(true);
        val response = mock(AuthenticationResponse.class);
        val entry = mock(LdapEntry.class);
        when(response.getLdapEntry()).thenReturn(entry);
        when(entry.getAttribute(anyString())).thenReturn(new LdapAttribute("attribute", "value"));
        val messages = new ArrayList<MessageDescriptor>();
        val config = new PasswordPolicyContext();
        config.setPasswordWarningNumberOfDays(5);
        h.handleWarning(new AccountState.DefaultWarning(ZonedDateTime.now(ZoneId.systemDefault()), 1),
            response, config, messages);
        assertEquals(2, messages.size());
    }

    @Test
    public void verifyWarningNoAttr() {
        val h = new OptionalWarningLdapAccountStateHandler();
        h.setDisplayWarningOnMatch(true);
        val response = mock(AuthenticationResponse.class);
        val config = new PasswordPolicyContext();
        config.setPasswordWarningNumberOfDays(5);
        val list = new ArrayList<MessageDescriptor>();
        h.handleWarning(
            new AccountState.DefaultWarning(ZonedDateTime.now(ZoneId.systemDefault()), 1),
            response, config, list);
        assertTrue(list.isEmpty());
        h.setWarnAttributeName("attribute");
        h.handleWarning(
            new AccountState.DefaultWarning(ZonedDateTime.now(ZoneId.systemDefault()), 1),
            response, config, list);
        assertTrue(list.isEmpty());
    }

    
    @Test
    public void verifyAlwaysWarningOnMatch() {
        val h = new OptionalWarningLdapAccountStateHandler();
        h.setWarnAttributeName("attribute");
        h.setWarningAttributeValue("value");
        h.setDisplayWarningOnMatch(true);
        val response = mock(AuthenticationResponse.class);
        val entry = mock(LdapEntry.class);
        when(response.getLdapEntry()).thenReturn(entry);
        when(entry.getAttribute(anyString())).thenReturn(new LdapAttribute("attribute", "value"));
        val messages = new ArrayList<MessageDescriptor>();
        val config = new PasswordPolicyContext();
        config.setAlwaysDisplayPasswordExpirationWarning(true);
        h.handleWarning(new AccountState.DefaultWarning(ZonedDateTime.now(ZoneId.systemDefault()), 1),
            response, config, messages);
        assertEquals(2, messages.size());
    }

    @Test
    public void verifyNoWarningOnMatch() {
        val h = new OptionalWarningLdapAccountStateHandler();
        h.setWarnAttributeName("attribute");
        h.setWarningAttributeValue("value");
        h.setDisplayWarningOnMatch(false);
        val response = mock(AuthenticationResponse.class);
        val entry = mock(LdapEntry.class);
        when(response.getLdapEntry()).thenReturn(entry);
        when(entry.getAttribute(anyString())).thenReturn(new LdapAttribute("attribute", "value"));
        val messages = new ArrayList<MessageDescriptor>();
        val config = new PasswordPolicyContext();
        config.setPasswordWarningNumberOfDays(5);
        h.handleWarning(new AccountState.DefaultWarning(ZonedDateTime.now(ZoneId.systemDefault()), 1),
            response, config, messages);
        assertEquals(0, messages.size());
    }
}
