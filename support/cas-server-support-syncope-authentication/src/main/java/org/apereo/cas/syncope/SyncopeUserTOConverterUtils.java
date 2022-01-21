package org.apereo.cas.syncope;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SyncopeUserTOConverterUtils}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@UtilityClass
public class SyncopeUserTOConverterUtils {
    private static final ObjectMapper MAPPER =
        JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Convert user as a JSON node into a map of details.
     *
     * @param user the user
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<Object>> convert(final JsonNode user) {
        val attributes = new HashMap<String, List<Object>>();

        if (user.has("securityQuestion") && !user.get("securityQuestion").isNull()) {
            attributes.put("syncopeUserSecurityQuestion", CollectionUtils.wrapList(user.get("securityQuestion").asText()));
        }

        attributes.put("syncopeUserStatus", CollectionUtils.wrapList(user.get("status").asText()));

        attributes.put("syncopeUserRealm", CollectionUtils.wrapList(user.get("realm").asText()));

        attributes.put("syncopeUserCreator", CollectionUtils.wrapList(user.get("creator").asText()));

        attributes.put("syncopeUserCreationDate", CollectionUtils.wrapList(user.get("creationDate").asText()));

        if (user.has("changePwdDate") && !user.get("changePwdDate").isNull()) {
            attributes.put("syncopeUserChangePwdDate", CollectionUtils.wrapList(user.get("changePwdDate").asText()));
        }

        if (user.has("lastLoginDate") && !user.get("lastLoginDate").isNull()) {
            attributes.put("syncopeUserLastLoginDate", CollectionUtils.wrapList(user.get("lastLoginDate").asText()));
        }

        collectListableAttribute(attributes, user, "roles", "syncopeUserRoles");
        collectListableAttribute(attributes, user, "dynRoles", "syncopeUserDynRoles");
        collectListableAttribute(attributes, user, "dynRealms", "syncopeUserDynRealms");
        
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
                r -> relationships.add(r.get("type").asText() + ';' + r.get("otherEndName").asText()));
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

    private static void collectListableAttribute(final Map<String, List<Object>> attributes,
                                                 final JsonNode user, final String syncopeAttribute,
                                                 final String casAttribute) {
        val values = user.has(syncopeAttribute)
            ? MAPPER.convertValue(user.get(syncopeAttribute), ArrayList.class)
            : CollectionUtils.wrapList();
        if (!values.isEmpty()) {
            attributes.put(casAttribute, values);
        }
    }
}
