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
     * @param user              the user
     * @param attributeMappings the attribute mappings
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<Object>> convert(final JsonNode user, final Map<String, String> attributeMappings) {
        val attributes = new HashMap<String, List<Object>>();

        if (user.has("securityQuestion") && !user.get("securityQuestion").isNull()) {
            var name = attributeMappings.getOrDefault("securityQuestion", "syncopeUserSecurityQuestion");
            attributes.put(name, CollectionUtils.wrapList(user.get("securityQuestion").asText()));
        }

        var name = attributeMappings.getOrDefault("key", "syncopeUserKey");
        attributes.put(name, CollectionUtils.wrapList(user.get("key").asText()));

        name = attributeMappings.getOrDefault("username", "username");
        attributes.put(name, CollectionUtils.wrapList(user.get("username").asText()));
        
        name = attributeMappings.getOrDefault("status", "syncopeUserStatus");
        attributes.put(name, CollectionUtils.wrapList(user.get("status").asText()));

        name = attributeMappings.getOrDefault("realm", "syncopeUserRealm");
        attributes.put(name, CollectionUtils.wrapList(user.get("realm").asText()));

        name = attributeMappings.getOrDefault("creator", "syncopeUserCreator");
        attributes.put(name, CollectionUtils.wrapList(user.get("creator").asText()));

        name = attributeMappings.getOrDefault("creationDate", "syncopeUserCreationDate");
        attributes.put(name, CollectionUtils.wrapList(user.get("creationDate").asText()));

        if (user.has("changePwdDate") && !user.get("changePwdDate").isNull()) {
            name = attributeMappings.getOrDefault("changePwdDate", "syncopeUserChangePwdDate");
            attributes.put(name, CollectionUtils.wrapList(user.get("changePwdDate").asText()));
        }

        if (user.has("lastLoginDate") && !user.get("lastLoginDate").isNull()) {
            name = attributeMappings.getOrDefault("lastLoginDate", "syncopeUserLastLoginDate");
            attributes.put(name, CollectionUtils.wrapList(user.get("lastLoginDate").asText()));
        }

        collectListableAttribute(attributes, user, "roles", "syncopeUserRoles", attributeMappings);
        collectListableAttribute(attributes, user, "dynRoles", "syncopeUserDynRoles", attributeMappings);
        collectListableAttribute(attributes, user, "dynRealms", "syncopeUserDynRealms", attributeMappings);

        if (user.has("memberships")) {
            val memberships = new ArrayList<>();
            user.get("memberships").forEach(member -> memberships.add(member.get("groupName").asText()));
            if (!memberships.isEmpty()) {
                name = attributeMappings.getOrDefault("memberships", "syncopeUserMemberships");
                attributes.put(name, memberships);
            }
        }

        if (user.has("dynMemberships")) {
            val dynMemberships = new ArrayList<>();
            user.get("dynMemberships").forEach(m -> dynMemberships.add(m.get("groupName").asText()));
            if (!dynMemberships.isEmpty()) {
                name = attributeMappings.getOrDefault("dynMemberships", "syncopeUserDynMemberships");
                attributes.put(name, dynMemberships);
            }
        }

        if (user.has("relationships")) {
            val relationships = new ArrayList<>();
            user.get("relationships").forEach(
                    r -> relationships.add(r.get("type").asText() + ';' + r.get("otherEndName").asText()));
            if (!relationships.isEmpty()) {
                name = attributeMappings.getOrDefault("relationships", "syncopeUserRelationships");
                attributes.put(name, relationships);
            }
        }

        if (user.has("plainAttrs")) {
            val prefix = attributeMappings.getOrDefault("plainAttrs", "syncopeUserAttr_");
            user.get("plainAttrs").forEach(attr -> {
                val attrName = prefix + attr.get("schema").asText();
                attributes.put(
                        attributeMappings.getOrDefault(attrName, attrName),
                        MAPPER.convertValue(attr.get("values"), ArrayList.class));
            });
        }
        if (user.has("derAttrs")) {
            val prefix = attributeMappings.getOrDefault("derAttrs", "syncopeUserAttr_");
            user.get("derAttrs").forEach(attr -> {
                val attrName = prefix + attr.get("schema").asText();
                attributes.put(
                        attributeMappings.getOrDefault(attrName, attrName),
                        MAPPER.convertValue(attr.get("values"), ArrayList.class));
            });
        }
        if (user.has("virAttrs")) {
            val prefix = attributeMappings.getOrDefault("virAttrs", "syncopeUserAttr_");
            user.get("virAttrs").forEach(attr -> {
                val attrName = prefix + attr.get("schema").asText();
                attributes.put(
                        attributeMappings.getOrDefault(attrName, attrName),
                        MAPPER.convertValue(attr.get("values"), ArrayList.class));
            });
        }

        return attributes;
    }

    private void collectListableAttribute(final Map<String, List<Object>> attributes,
            final JsonNode user, final String syncopeAttribute,
            final String casAttribute,
            final Map<String, String> attributeMappings) {
        val values = user.has(syncopeAttribute)
                ? MAPPER.convertValue(user.get(syncopeAttribute), ArrayList.class)
                : CollectionUtils.wrapList();
        if (!values.isEmpty()) {
            val name = attributeMappings.getOrDefault(syncopeAttribute, casAttribute);
            attributes.put(name, values);
        }
    }
}
