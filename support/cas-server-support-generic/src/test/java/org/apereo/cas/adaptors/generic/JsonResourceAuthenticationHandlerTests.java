package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
@Tag("AuthenticationHandler")
class JsonResourceAuthenticationHandlerTests {
    private final JsonResourceAuthenticationHandler handler;

    JsonResourceAuthenticationHandlerTests() throws Exception {
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
        acct.setWarnings(CollectionUtils.wrapList("hello.world", "test.message"));
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

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setLocation(RegexUtils.MATCH_NOTHING_PATTERN.pattern());
        acct.setStatus(CasUserAccount.AccountStatus.OK);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("badlocation", acct);

        acct = new CasUserAccount();
        acct.setPassword("Mellon");
        acct.setAvailability("2020-10-20~2020-11-20");
        acct.setStatus(CasUserAccount.AccountStatus.OK);
        acct.setAttributes(CollectionUtils.wrap("firstName",
            CollectionUtils.wrapList("Apereo"), "lastName",
            CollectionUtils.wrapList("CAS")));
        accounts.put("badtime", acct);

        val resource = new FileSystemResource(Files.createTempFile("account", ".json").toFile());

        val mapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

        mapper
            .findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .writerWithDefaultPrettyPrinter();
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        mapper.writeValue(resource.getFile(), accounts);
        this.handler = new JsonResourceAuthenticationHandler(null,
            PrincipalFactoryUtils.newPrincipalFactory(), null, resource);
        this.handler.setPasswordPolicyConfiguration(new PasswordPolicyContext(15));

        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOkAccountFromExternalFile() throws Throwable {
        val resource = new ClassPathResource("sample-users.json");
        val jsonHandler = new JsonResourceAuthenticationHandler(null,
            PrincipalFactoryUtils.newPrincipalFactory(), null, resource);
        assertThrows(FailedLoginException.class,
            () -> jsonHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "bad-password"), mock(Service.class)));
        val result = jsonHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"), mock(Service.class));
        assertNotNull(result);
        assertEquals(1, result.getWarnings().size());
        assertEquals("casuser", result.getPrincipal().getId());
        assertFalse(result.getPrincipal().getAttributes().isEmpty());
        assertTrue(result.getPrincipal().getAttributes().containsKey("firstName"));
        assertEquals("Apereo", result.getPrincipal().getAttributes().get("firstName").getFirst());
    }

    @Test
    void verifyOkAccountFromExternalFileWithEncodedPassword() throws Throwable {
        val resource = new ClassPathResource("sample-users.json");
        val jsonHandler = new JsonResourceAuthenticationHandler(null,
            PrincipalFactoryUtils.newPrincipalFactory(), null, resource);

        val p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        p.setEncodingAlgorithm("MD5");
        p.setCharacterEncoding("UTF-8");
        jsonHandler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(p, mock(ApplicationContext.class)));

        assertNotNull(jsonHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casmd5", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyInvalidAccounts() throws Throwable {
        val resource = new FileSystemResource(Files.createTempFile("bad-account", ".json").toFile());
        FileUtils.write(resource.getFile(), "invalid-data", StandardCharsets.UTF_8);
        val jsonHandler = new JsonResourceAuthenticationHandler(null,
            PrincipalFactoryUtils.newPrincipalFactory(), null, resource);
        assertThrows(PreventedException.class, () -> jsonHandler.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpiring", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyExpiringAccount() throws Throwable {
        val result = handler.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpiring", "Mellon"), mock(Service.class));
        assertFalse(result.getWarnings().isEmpty());
    }

    @Test
    void verifyOkAccount() throws Throwable {
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyNotFoundAccount() {
        assertThrows(AccountNotFoundException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("nobody", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyExpiredAccount() {
        assertThrows(AccountExpiredException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casexpired", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyDisabledAccount() {
        assertThrows(AccountDisabledException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casdisabled", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyLockedAccount() {
        assertThrows(AccountLockedException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("caslocked", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyMustChangePswAccount() {
        assertThrows(AccountPasswordMustChangeException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casmustchange", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyInvalidLocation() {
        assertThrows(InvalidLoginLocationException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("badlocation", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyInvalidTime() {
        assertThrows(InvalidLoginTimeException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("badtime", "Mellon"), mock(Service.class)));
    }

}
