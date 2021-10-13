package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.SyncopeAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SuppressWarnings("unused")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    SyncopeAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CasPersonDirectoryTestConfiguration.class
},
    properties = "cas.authn.syncope.url=http://localhost:8095")
@ResourceLock("Syncope")
@Tag("AuthenticationHandler")
public class SyncopeAuthenticationHandlerTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("syncopeAuthenticationHandler")
    private AuthenticationHandler syncopeAuthenticationHandler;

    @Test
    @SuppressWarnings("JavaUtilDate")
    public void verifyHandlerPasses() {
        val user = MAPPER.createObjectNode();
        user.put("username", "casuser");
        user.putArray("roles").add("role1");
        user.putArray("dynRoles").add("DynRole1");
        user.putArray("dynRealms").add("Realm1");
        user.putArray("memberships").add(MAPPER.createObjectNode()
            .put("groupName", "G1"));
        user.putArray("dynMemberships").add(MAPPER.createObjectNode().
            put("groupName", "G1"));
        user.putArray("relationships").add(MAPPER.createObjectNode()
            .put("type", "T1").put("otherEndName", "Other1"));

        val plainAttrs = MAPPER.createObjectNode();
        plainAttrs.put("schema", "S1");
        plainAttrs.putArray("values").add("V1");
        user.putArray("plainAttrs").add(plainAttrs);

        val derAttrs = MAPPER.createObjectNode();
        derAttrs.put("schema", "S2");
        derAttrs.putArray("values").add("V2");
        user.putArray("derAttrs").add(derAttrs);

        val virAttrs = MAPPER.createObjectNode();
        virAttrs.put("schema", "S3");
        virAttrs.putArray("values").add("V3");
        user.putArray("virAttrs").add(virAttrs);

        user.put("securityQuestion", "Q1");
        user.put("status", "OK");
        user.put("realm", "Master");
        user.put("creator", "admin");
        user.put("creationDate", new Date().toString());
        user.put("changePwdDate", new Date().toString());
        user.put("lastLoginDate", new Date().toString());

        @Cleanup("stop")
        val webserver = startMockSever(user);
        assertDoesNotThrow(() ->
            syncopeAuthenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password")));
    }

    @Test
    public void verifyHandlerMustChangePassword() {
        val user = MAPPER.createObjectNode();
        user.put("username", "casuser");
        user.put("mustChangePassword", true);
        @Cleanup("stop")
        val webserver = startMockSever(user);

        assertThrows(AccountPasswordMustChangeException.class,
            () -> syncopeAuthenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password")));
    }

    @Test
    public void verifyHandlerSuspended() {
        val user = MAPPER.createObjectNode();
        user.put("username", "casuser");
        user.put("suspended", true);
        @Cleanup("stop")
        val webserver = startMockSever(user);

        assertThrows(AccountDisabledException.class,
            () -> syncopeAuthenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password")));
    }

    @SneakyThrows
    private static MockWebServer startMockSever(final JsonNode user) {
        val data = MAPPER.writeValueAsString(user);
        val webServer = new MockWebServer(8095,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        webServer.start();
        return webServer;
    }
}
