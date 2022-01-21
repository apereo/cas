package org.apereo.cas.syncope.authentication;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
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
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.SyncopeAuthenticationConfiguration;
import org.apereo.cas.config.SyncopePersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * This is {@link BaseSyncopeTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
public abstract class BaseSyncopeTests {

    protected static final ObjectMapper MAPPER =
        JacksonObjectMapperFactory.builder().defaultTypingEnabled(true).build().toObjectMapper();

    @SneakyThrows
    protected static MockWebServer startMockSever(final JsonNode json, final HttpStatus status, final int port) {
        val data = MAPPER.writeValueAsString(json);
        val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE, status);
        webServer.start();
        return webServer;
    }

    @SuppressWarnings("JavaUtilDate")
    protected static ObjectNode user() {
        val user = MAPPER.createObjectNode();
        user.put("username", "casuser");
        user.putArray("roles").add("role1");
        user.putArray("dynRoles").add("DynRole1");
        user.putArray("dynRealms").add("Realm1");
        user.putArray("memberships").add(MAPPER.createObjectNode()
            .put("groupName", "G1"));
        user.putArray("dynMemberships").add(MAPPER.createObjectNode()
            .put("groupName", "G1"));
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

        return user;
    }

    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        SyncopeAuthenticationConfiguration.class,
        SyncopePersonDirectoryConfiguration.class,

        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
