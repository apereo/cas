package org.apereo.cas.scim.v2;

import module java.base;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.scim.v2.access.DefaultScimService;
import com.sun.net.httpserver.HttpServer;
import de.captaingoldfish.scim.sdk.common.constants.HttpHeader;
import de.captaingoldfish.scim.sdk.common.constants.ResourceTypeNames;
import de.captaingoldfish.scim.sdk.common.constants.SchemaUris;
import de.captaingoldfish.scim.sdk.common.resources.Group;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Member;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScimServiceGroupMembersTests}.
 *
 * @author Jerome LELEU
 * @since 8.1.0
 */
@Tag("SCIM")
class ScimServiceGroupMembersTests {
    private static final String USER_ID = "scim_user_01";

    private static final String OTHER_USER_ID = "scim_user_02";

    @Test
    void verifyGroupUpdatePreservesExistingMembersAndAddsMissingMembers() throws Exception {
        val existingGroup = Group.builder()
            .id("scim_group_01")
            .displayName("Engineering")
            .members(List.of(
                Member.builder()
                    .value(USER_ID)
                    .display("CAS User")
                    .type(ResourceTypeNames.USER)
                    .ref("https://scim.example.org/scim/v2/Users/" + USER_ID)
                    .build(),
                Member.builder()
                    .value(OTHER_USER_ID)
                    .display("Other User")
                    .type(ResourceTypeNames.USER)
                    .build()))
            .build();
        val existingGroups = """
            {"schemas": ["%s"], "totalResults": 1, "startIndex": 1, "itemsPerPage": 1, "Resources": [%s]}
            """.formatted(SchemaUris.LIST_RESPONSE_URI, existingGroup);

        val updatedGroup = new AtomicReference<String>();
        val server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            var body = existingGroups;
            if (HttpMethod.PUT.matches(exchange.getRequestMethod())) {
                body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                updatedGroup.set(body);
            }
            val response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(HttpHeader.CONTENT_TYPE_HEADER, HttpHeader.SCIM_CONTENT_TYPE);
            exchange.sendResponseHeaders(HttpStatus.OK.value(), response.length);
            try (val output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        try {
            val properties = new ScimProperties().setTarget("http://127.0.0.1:%s".formatted(server.getAddress().getPort()));
            val scimService = new DefaultScimService(properties);
            val newMember = Member.builder().value("scim_user_03").type(ResourceTypeNames.USER).build();
            val group = Group.builder()
                .displayName("Engineering")
                .members(List.of(Member.builder().value(USER_ID).type(ResourceTypeNames.USER).build(), newMember, newMember))
                .build();
            assertTrue(scimService.createOrUpdateGroups(scimService.getScimRequestBuilder(Optional.empty()), group));

            val members = JsonHelper.readJsonDocument(updatedGroup.get(), Group.class).getMembers();
            assertTrue(members.containsAll(existingGroup.getMembers()), members::toString);
            assertTrue(members.contains(newMember));
            assertEquals(3, members.size());
        } finally {
            server.stop(0);
        }
    }
}
