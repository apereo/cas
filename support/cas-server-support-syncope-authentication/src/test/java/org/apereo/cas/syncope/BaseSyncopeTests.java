package org.apereo.cas.syncope;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSyncopeAutoConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * This is {@link BaseSyncopeTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
public abstract class BaseSyncopeTests {
    protected static final ObjectMapper MAPPER =
        JacksonObjectMapperFactory.builder().defaultTypingEnabled(true).build().toObjectMapper();

    protected static MockWebServer startMockSever(final JsonNode json, final HttpStatus status,
                                                  final int port) throws Exception {
        val data = MAPPER.writeValueAsString(json);
        val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE, status);
        webServer.start();
        return webServer;
    }

    protected static ObjectNode user() {
        val user = MAPPER.createObjectNode();
        user.put("key", UUID.randomUUID().toString());
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
        WebMvcAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasSyncopeAutoConfiguration.class,

        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
