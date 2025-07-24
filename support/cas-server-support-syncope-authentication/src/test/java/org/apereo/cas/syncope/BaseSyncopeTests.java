package org.apereo.cas.syncope;

import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSyncopeAutoConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
        return webServer.start();
    }

    protected static ObjectNode user() {
        val user = MAPPER.createObjectNode();
        user.put("key", UUID.randomUUID().toString());
        user.put("username", "casuser");
        user.putArray("roles").add("role1");
        user.putArray("dynRoles").add("DynRole1");
        user.putArray("dynRealms").add("Realm1");
        user.putArray("memberships")
            .add(MAPPER.createObjectNode().put("groupName", "G1"));
        user.putArray("dynMemberships")
            .add(MAPPER.createObjectNode().put("groupName", "G1"));
        user.putArray("relationships")
            .add(MAPPER.createObjectNode().put("type", "T1").put("otherEndName", "Other1"));

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

    protected static ObjectNode userForMembershipsTypeExtension() {
        val user = MAPPER.createObjectNode();
        user.put("key", UUID.randomUUID().toString());
        user.put("username", "casuser");
        user.putArray("roles").add("role1");
        user.putArray("dynRoles").add("DynRole1");
        user.putArray("dynRealms").add("Realm1");
        user.putArray("dynMemberships").add(MAPPER.createObjectNode()
            .put("groupName", "G1"));
        user.putArray("relationships").add(MAPPER.createObjectNode()
            .put("type", "T1").put("otherEndName", "Other1"));

        val memberships = user.putArray("memberships");
        val membershipsInfo = MAPPER.createObjectNode().put("groupName", "G1");
        val membershipPlainAttrs = membershipsInfo.putArray("plainAttrs");

        val plainAttrNode1 = MAPPER.createObjectNode();
        plainAttrNode1.put("schema", "testSchema1");
        val values1 = plainAttrNode1.putArray("values");
        values1.add("valueSchema1");
        membershipPlainAttrs.add(plainAttrNode1);

        val plainAttrNode2 = MAPPER.createObjectNode();
        plainAttrNode2.put("schema", "testSchema2");
        val values2 = plainAttrNode2.putArray("values");
        values2.add("valueSchema2");
        membershipPlainAttrs.add(plainAttrNode2);

        memberships.add(membershipsInfo);

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
        CasSyncopeAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasPasswordlessAuthenticationAutoConfiguration.class,
        CasPasswordManagementAutoConfiguration.class,
        CasAccountManagementWebflowAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @SpringBootTestAutoConfigurations
    @Import({CasRegisteredServicesTestConfiguration.class, CasAuthenticationEventExecutionPlanTestConfiguration.class})
    public static class SharedTestConfiguration {
    }
}
