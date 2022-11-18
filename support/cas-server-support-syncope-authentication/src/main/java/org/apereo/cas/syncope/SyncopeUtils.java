package org.apereo.cas.syncope;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.syncope.BaseSyncopeSearchProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link SyncopeUtils}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@UtilityClass
@Slf4j
public class SyncopeUtils {
    private static final ObjectMapper MAPPER =
        JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Convert user as a JSON node into a map of details.
     *
     * @param user the user
     * @return the map
     */
    public static Map<String, List<Object>> convertFromUserEntity(final JsonNode user) {
        val attributes = new HashMap<String, List<Object>>();

        if (user.has("securityQuestion") && !user.get("securityQuestion").isNull()) {
            attributes.put("syncopeUserSecurityQuestion", CollectionUtils.wrapList(user.get("securityQuestion").asText()));
        }

        attributes.put("syncopeUserKey", CollectionUtils.wrapList(user.get("key").asText()));
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

    /**
     * Syncope search.
     *
     * @param properties the properties
     * @param user       the user
     * @return the optional
     */
    public static List<Map<String, List<Object>>> syncopeUserSearch(final BaseSyncopeSearchProperties properties, final String user) {
        HttpResponse response = null;
        try {
            val filter = properties.getSearchFilter().replace("{user}", user).replace("{0}", user);
            val fiql = EncodingUtils.urlEncode(filter);
            val syncopeRestUrl = StringUtils.appendIfMissing(properties.getUrl(), "/")
                                 + "rest/users/?page=1&size=1&details=true&fiql=" + fiql;
            LOGGER.debug("Executing Syncope search via [{}]", syncopeRestUrl);
            val requestHeaders = new LinkedHashMap<String, String>();
            requestHeaders.put("X-Syncope-Domain", properties.getDomain());
            requestHeaders.putAll(properties.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .headers(requestHeaders)
                .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());
            if (HttpStatus.resolve(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                val entity = response.getEntity();
                return FunctionUtils.doUnchecked(() -> {
                    val result = EntityUtils.toString(entity);
                    LOGGER.debug("Received user entity as [{}]", result);
                    val it = Optional.of(MAPPER.readTree(result))
                        .filter(sr -> sr.has("result"))
                        .map(sr -> sr.get("result"))
                        .map(JsonNode::iterator)
                        .orElse(Collections.emptyIterator());
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                        .map(SyncopeUtils::convertFromUserEntity)
                        .collect(Collectors.toList());
                });
            }
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }

    /**
     * Convert to user create entity map.
     *
     * @param principal the principal
     * @param realm     the realm
     * @return the map
     */
    public static Map<String, Object> convertToUserCreateEntity(final Principal principal, final String realm) {
        val entity = new LinkedHashMap<String, Object>();
        entity.put("_class", "org.apache.syncope.common.lib.request.UserCR");
        entity.put("realm", StringUtils.prependIfMissing(realm, "/"));
        entity.put("username", principal.getId());

        val plainAttrs = new ArrayList<Map<String, Object>>();
        principal.getAttributes()
            .entrySet()
            .stream()
            .filter(entry -> !"username".equals(entry.getKey()) && !"password".equals(entry.getKey()))
            .forEach(entry -> plainAttrs.add(Map.of("schema", entry.getKey(), "values", CollectionUtils.toCollection(entry.getValue()))));
        entity.put("plainAttrs", plainAttrs);
        return entity;
    }

    /**
     * Convert to user create entity map.
     *
     * @param userProperties the user properties
     * @param credential     the credential
     * @param realm          the realm
     * @return the map
     */
    public static Map<String, Object> convertToUserCreateEntity(final Map<String, ?> userProperties,
                                                                final UsernamePasswordCredential credential,
                                                                final String realm) {
        val entity = new LinkedHashMap<String, Object>();
        entity.put("_class", "org.apache.syncope.common.lib.request.UserCR");
        entity.put("realm", StringUtils.prependIfMissing(realm, "/"));
        entity.put("username", credential.getUsername());
        entity.put("password", credential.getPassword());

        val plainAttrs = new ArrayList<Map<String, Object>>();
        userProperties
            .entrySet()
            .stream()
            .filter(entry -> !"username".equals(entry.getKey()) && !"password".equals(entry.getKey()))
            .forEach(entry -> plainAttrs.add(Map.of("schema", entry.getKey(), "values", CollectionUtils.toCollection(entry.getValue()))));
        entity.put("plainAttrs", plainAttrs);
        return entity;
    }

    /**
     * Convert to user update entity map.
     *
     * @param realm          the realm
     * @return the map
     */
    public static Map<String, Object> convertToUserUpdateEntity(final Principal principal,
                                                                final String realm) {
        val entity = new LinkedHashMap<String, Object>();
        entity.put("_class", "org.apache.syncope.common.lib.request.UserUR");
        entity.put("key", principal.getId());
        entity.put("realm", Map.of("operation", "ADD_REPLACE", "value", realm));

        val plainAttrs = new ArrayList<Map<String, Object>>();
        principal.getAttributes()
            .entrySet()
            .stream()
            .filter(entry -> !"username".equals(entry.getKey()) && !"password".equals(entry.getKey()))
            .forEach(entry -> {
                val attribute = Map.of("operation", "ADD_REPLACE",
                    "attr", Map.of("schema", entry.getKey(), "values", CollectionUtils.toCollection(entry.getValue())));
                plainAttrs.add(attribute);
            });
        entity.put("plainAttrs", plainAttrs);
        return entity;
    }

}
