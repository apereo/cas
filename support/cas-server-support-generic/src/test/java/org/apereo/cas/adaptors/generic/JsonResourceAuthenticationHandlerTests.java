package org.apereo.cas.adaptors.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JsonResourceAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class JsonResourceAuthenticationHandlerTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Resource resource;
    private JsonResourceAuthenticationHandler handler;

    public JsonResourceAuthenticationHandlerTests() throws Exception {
        final Map<String, CasUserAccount> accounts = new LinkedHashMap<>();

        CasUserAccount acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setExpirationDate(LocalDate.now(ZoneOffset.UTC).plusWeeks(2));
        acct.setAttributes(CollectionUtils.wrap("firstName", "Apereo", "lastName", "CAS"));
        accounts.put("casexpiring", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.OK);
        acct.setAttributes(CollectionUtils.wrap("firstName", "Apereo", "lastName", "CAS"));
        accounts.put("casuser", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.DISABLED);
        acct.setAttributes(CollectionUtils.wrap("firstName", "Apereo", "lastName", "CAS"));
        accounts.put("casdisabled", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.MUST_CHANGE_PASSWORD);
        acct.setAttributes(CollectionUtils.wrap("firstName", "Apereo", "lastName", "CAS"));
        accounts.put("casmustchange", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.LOCKED);
        acct.setAttributes(CollectionUtils.wrap("firstName", "Apereo", "lastName", "CAS"));
        accounts.put("caslocked", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.EXPIRED);
        acct.setAttributes(CollectionUtils.wrap("firstName", "Apereo", "lastName", "CAS"));
        accounts.put("casexpired", acct);

        this.resource = new FileSystemResource(File.createTempFile("account", ".json"));

        final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

        mapper
            .findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
            .writerWithDefaultPrettyPrinter()
            .writeValue(resource.getFile(), accounts);
        this.handler = new JsonResourceAuthenticationHandler(null, mock(ServicesManager.class),
            new DefaultPrincipalFactory(), null, this.resource);
        this.handler.setPasswordPolicyConfiguration(new PasswordPolicyConfiguration(15));
    }

    @Test
    public void verifyExpiringAccount() throws Exception {
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpiring", "Mellon");
        final AuthenticationHandlerExecutionResult result = handler.authenticate(c);
        assertFalse(result.getWarnings().isEmpty());
    }

    @Test
    public void verifyOkAccount() throws Exception {
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        assertNotNull(handler.authenticate(c));
    }

    @Test
    public void verifyNotFoundAccount() throws Exception {
        this.thrown.expect(AccountNotFoundException.class);
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("nobody", "Mellon");
        handler.authenticate(c);
    }

    @Test
    public void verifyExpiredAccount() throws Exception {
        this.thrown.expect(AccountExpiredException.class);
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpired", "Mellon");
        handler.authenticate(c);
    }

    @Test
    public void verifyDisabledAccount() throws Exception {
        this.thrown.expect(AccountDisabledException.class);
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casdisabled", "Mellon");
        handler.authenticate(c);
    }

    @Test
    public void verifyLockedAccount() throws Exception {
        this.thrown.expect(AccountLockedException.class);
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("caslocked", "Mellon");
        handler.authenticate(c);
    }

    @Test
    public void verifyMustChangePswAccount() throws Exception {
        this.thrown.expect(AccountPasswordMustChangeException.class);
        final UsernamePasswordCredential c =
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casmustchange", "Mellon");
        handler.authenticate(c);
    }
}
