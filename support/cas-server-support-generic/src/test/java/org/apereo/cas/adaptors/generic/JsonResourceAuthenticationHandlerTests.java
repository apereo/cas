package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JsonResourceAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("FileSystem")
public class JsonResourceAuthenticationHandlerTests {
    private final JsonResourceAuthenticationHandler handler;

    @SneakyThrows
    public JsonResourceAuthenticationHandlerTests() {
        val accounts = new LinkedHashMap<String, CasUserAccount>();

        var acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setExpirationDate(LocalDate.now(ZoneOffset.UTC).plusWeeks(2));
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("casexpiring", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.OK);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("casuser", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.DISABLED);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("casdisabled", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.MUST_CHANGE_PASSWORD);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("casmustchange", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.LOCKED);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("caslocked", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setStatus(CasUserAccount.AccountStatus.EXPIRED);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("casexpired", acct);

        val resource = new FileSystemResource(File.createTempFile("account", ".json"));

        val mapper = Jackson2ObjectMapperBuilder.json()
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
            PrincipalFactoryUtils.newPrincipalFactory(), null, resource);
        this.handler.setPasswordPolicyConfiguration(new PasswordPolicyContext(15));
    }

    @Test
    @SneakyThrows
    public void verifyOkAccountFromExternalFile() {
        val resource = new ClassPathResource("sample-users.json");
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val jsonHandler = new JsonResourceAuthenticationHandler(null, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), null, resource);
        val result = jsonHandler.authenticate(creds);
        assertNotNull(result);
        assertEquals("casuser", result.getPrincipal().getId());
        assertFalse(result.getPrincipal().getAttributes().isEmpty());
        assertTrue(result.getPrincipal().getAttributes().containsKey("firstName"));
        assertEquals("Apereo", result.getPrincipal().getAttributes().get("firstName").get(0));
    }
    
    @Test
    @SneakyThrows
    public void verifyExpiringAccount() {
        val result = handler.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpiring", "Mellon"));
        assertFalse(result.getWarnings().isEmpty());
    }

    @Test
    @SneakyThrows
    public void verifyOkAccount() {
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }

    @Test
    public void verifyNotFoundAccount() {
        assertThrows(AccountNotFoundException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("nobody", "Mellon")));
    }

    @Test
    public void verifyExpiredAccount() {
        assertThrows(AccountExpiredException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpired", "Mellon")));
    }

    @Test
    public void verifyDisabledAccount() {
        assertThrows(AccountDisabledException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casdisabled", "Mellon")));
    }

    @Test
    public void verifyLockedAccount() {
        assertThrows(AccountLockedException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("caslocked", "Mellon")));
    }

    @Test
    public void verifyMustChangePswAccount() {
        assertThrows(AccountPasswordMustChangeException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casmustchange", "Mellon")));
    }
}
