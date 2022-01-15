package org.apereo.cas.syncope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

public final class SyncopeUserTOConverter {

    private static final ObjectMapper MAPPER =
            JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();

    @SuppressWarnings("unchecked")
    public static Map<String, List<Object>> convert(final JsonNode user) {
        val attributes = new HashMap<String, List<Object>>();

        if (user.has("securityQuestion") && !user.get("securityQuestion").isNull()) {
            attributes.put("syncopeUserSecurityQuestion", List.of(user.get("securityQuestion").asText()));
        }

        attributes.put("syncopeUserStatus", List.of(user.get("status").asText()));

        attributes.put("syncopeUserRealm", List.of(user.get("realm").asText()));

        attributes.put("syncopeUserCreator", List.of(user.get("creator").asText()));

        attributes.put("syncopeUserCreationDate", List.of(user.get("creationDate").asText()));

        if (user.has("changePwdDate") && !user.get("changePwdDate").isNull()) {
            attributes.put("syncopeUserChangePwdDate", List.of(user.get("changePwdDate").asText()));
        }

        if (user.has("lastLoginDate") && !user.get("lastLoginDate").isNull()) {
            attributes.put("syncopeUserLastLoginDate", List.of(user.get("lastLoginDate").asText()));
        }

        val roles = user.has("roles")
                ? MAPPER.convertValue(user.get("roles"), ArrayList.class)
                : List.of();
        if (!roles.isEmpty()) {
            attributes.put("syncopeUserRoles", roles);
        }

        val dynRoles = user.has("dynRoles")
                ? MAPPER.convertValue(user.get("dynRoles"), ArrayList.class)
                : List.of();
        if (!dynRoles.isEmpty()) {
            attributes.put("syncopeUserDynRoles", dynRoles);
        }

        val dynRealms = user.has("dynRealms")
                ? MAPPER.convertValue(user.get("dynRealms"), ArrayList.class)
                : List.of();
        if (!dynRealms.isEmpty()) {
            attributes.put("syncopeUserDynRealms", dynRealms);
        }

        if (user.has("memberships")) {
            val memberships = new ArrayList<>();
            user.get("memberships").forEach(m -> memberships.add(m.get("groupName").asText()));
            if (!memberships.isEmpty()) {
                attributes.put("syncopeUserMemberships", memberships);
            }
        }

        if (user.has("dynMemberships")) {
            val dynMemberships = new ArrayList<>();
            user.get("dynMemberships").forEach(m -> dynMemberships.add(m.get("groupName").asText()));
            if (!dynMemberships.isEmpty()) {
                attributes.put("syncopeUserDynMemberships", dynMemberships);
            }
        }

        if (user.has("relationships")) {
            val relationships = new ArrayList<>();
            user.get("relationships").forEach(
                    r -> relationships.add(r.get("type").asText() + ";" + r.get("otherEndName").asText()));
            if (!relationships.isEmpty()) {
                attributes.put("syncopeUserRelationships", relationships);
            }
        }

        if (user.has("plainAttrs")) {
            user.get("plainAttrs").forEach(a -> attributes.put(
                    "syncopeUserAttr_" + a.get("schema").asText(),
                    MAPPER.convertValue(a.get("values"), ArrayList.class)));
        }
        if (user.has("derAttrs")) {
            user.get("derAttrs").forEach(a -> attributes.put(
                    "syncopeUserAttr_" + a.get("schema").asText(),
                    MAPPER.convertValue(a.get("values"), ArrayList.class)));
        }
        if (user.has("virAttrs")) {
            user.get("virAttrs").forEach(a -> attributes.put(
                    "syncopeUserAttr_" + a.get("schema").asText(),
                    MAPPER.convertValue(a.get("values"), ArrayList.class)));
        }

        return attributes;
    }

    private SyncopeUserTOConverter() {
        // private constructor for static utility class
    }
}
