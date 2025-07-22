package org.apereo.cas.syncope;

import java.util.Locale;
import javax.net.ssl.SSLHandshakeException;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.syncope.BaseSyncopeSearchProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
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
     * @param user              the user
     * @param attributeMappings the attribute mappings
     * @return the map
     */
    public static Map<String, List<Object>> convertFromUserEntity(final JsonNode user,
                                                                  final Map<String, String> attributeMappings) {
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
            val memberships = mapSyncopeGroupMemberhips(user);
            if (!memberships.isEmpty()) {
                name = attributeMappings.getOrDefault("memberships", "syncopeUserMemberships");
                attributes.put(name, new ArrayList<>(memberships));
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

        mapSyncopeUserAttributes(user, "plainAttrs", attributeMappings, attributes);
        mapSyncopeUserAttributes(user, "derAttrs", attributeMappings, attributes);
        mapSyncopeUserAttributes(user, "virAttrs", attributeMappings, attributes);

        return attributes;
    }

    private List<Map<String, String>> mapSyncopeGroupMemberhips(final JsonNode user) {
        val memberships = new ArrayList<Map<String, String>>();
        user.get("memberships").forEach(member -> {
            val membershipInfo = new HashMap<String, String>();
            membershipInfo.put("groupName", member.get("groupName").asText());
            if (member.has("plainAttrs")) {
                member.get("plainAttrs").forEach(attr ->
                    membershipInfo.put(attr.get("schema").asText(), attr.get("values").toString())
                );
                memberships.add(membershipInfo);
            }
        });
        return memberships;
    }

    private void mapSyncopeUserAttributes(final JsonNode user, final String attributeName,
                                          final Map<String, String> attributeMappings,
                                          final Map<String, List<Object>> attributes) {
        if (user.has(attributeName)) {
            val prefix = attributeMappings.getOrDefault(attributeName, "syncopeUserAttr_");
            user.get(attributeName).forEach(attr -> {
                val attrName = prefix + attr.get("schema").asText();
                attributes.put(
                    attributeMappings.getOrDefault(attrName, attrName),
                    MAPPER.convertValue(attr.get("values"), ArrayList.class));
            });
        }
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
            val syncopeRestUrl = StringUtils.appendIfMissing(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()), "/")
                + "rest/users/?page=1&size=1&details=true&fiql=" + fiql;
            LOGGER.debug("Executing Syncope search via [{}]", syncopeRestUrl);
            val requestHeaders = new LinkedHashMap<String, String>();
            requestHeaders.put("X-Syncope-Domain", properties.getDomain());
            requestHeaders.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .headers(requestHeaders)
                .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getReasonPhrase());
            if (HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                val entity = ((HttpEntityContainer) response).getEntity();
                return FunctionUtils.doUnchecked(() -> {
                    val result = EntityUtils.toString(entity);
                    LOGGER.debug("Received user entity as [{}]", result);
                    val it = Optional.of(MAPPER.readTree(result))
                        .filter(sr -> sr.has("result"))
                        .map(sr -> sr.get("result"))
                        .map(JsonNode::iterator)
                        .orElse(Collections.emptyIterator());
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                        .map(node -> SyncopeUtils.convertFromUserEntity(node, properties.getAttributeMappings()))
                        .collect(Collectors.toList());
                });
            }
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }


    /**
     * Syncope user groups search.
     *
     * @param properties the properties
     * @param user       the user
     * @return the list
     */
    public static List<Map<String, List<Object>>> syncopeUserGroupsSearch(final BaseSyncopeSearchProperties properties,
                                                                          final String user) {

        HttpResponse response = null;
        try {
            val fiql = EncodingUtils.urlEncode("$member==%s".formatted(user));
            val syncopeRestUrl = StringUtils.appendIfMissing(
                SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()), "/")
                + "rest/groups/?page=1&size=50&details=true&fiql=" + fiql;
            LOGGER.debug("Executing Syncope user group search via [{}]", syncopeRestUrl);
            val requestHeaders = new LinkedHashMap<String, String>();
            requestHeaders.put("X-Syncope-Domain", properties.getDomain());
            requestHeaders.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
                .headers(requestHeaders)
                .build();

            response = Objects.requireNonNull(HttpUtils.execute(exec));
            if (Objects.requireNonNull(HttpStatus.resolve(response.getCode())).is2xxSuccessful()) {
                val entity = ((HttpEntityContainer) response).getEntity();
                return FunctionUtils.doUnchecked(() -> {
                    val result = EntityUtils.toString(entity);
                    val it = Optional.of(MAPPER.readTree(result))
                        .filter(sr -> sr.has("result"))
                        .map(sr -> sr.get("result"))
                        .map(JsonNode::iterator)
                        .orElse(Collections.emptyIterator());
                    val groups = new ArrayList<>();
                    StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                        .forEach(node -> convertFromGroupEntity(node, groups));
                    val name = properties.getAttributeMappings().getOrDefault("groups", "syncopeUserGroups");
                    val attributes = new HashMap<String, List<Object>>();
                    attributes.put(name, groups);
                    return List.of(attributes);
                });
            }
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();

    }

    private static void convertFromGroupEntity(final JsonNode group,
                                               final List<Object> attributes) {
        val groupAttrs = new HashMap<String, String>();
        groupAttrs.put("groupName", group.get("name").asText());
        group.get("plainAttrs").forEach(attr -> groupAttrs.put(attr.get("schema").asText(), attr.get("values").toString()));
        attributes.add(groupAttrs);
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
     * @param realm the realm
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

    /**
     * Execute http request.
     *
     * @param execution http request
     * @return http response
     */
    public static HttpResponse execute(final HttpExecutionRequest execution) {
        val uri = HttpUtils.buildHttpUri(execution.getUrl().trim(), execution.getParameters());
        val request = HttpUtils.getHttpRequestByMethod(execution.getMethod().name().toLowerCase(Locale.ENGLISH).trim(),
                execution.getEntity(), uri);
        try {
            val expressionResolver = SpringExpressionLanguageValueResolver.getInstance();
            execution.getHeaders().forEach((key, value) -> {
                val headerValue = expressionResolver.resolve(value);
                val headerKey = expressionResolver.resolve(key);
                request.addHeader(headerKey, headerValue);
            });
            HttpUtils.prepareHttpRequest(request, execution);
            val client = HttpUtils.getHttpClient(execution);
            return FunctionUtils.doAndRetry(retryContext -> {
                LOGGER.trace("Sending HTTP request to [{}]. Attempt: [{}]", request.getUri(), retryContext.getRetryCount());
                val res = client.execute(request);
                if (res == null || org.springframework.http.HttpStatus.valueOf(res.getCode()).isError()) {
                    val maxAttempts = (Integer) retryContext.getAttribute("retry.maxAttempts");
                    if (maxAttempts != null && retryContext.getRetryCount() != maxAttempts - 1) {
                        return res;
                    }
                }
                return res;
            }, execution.getMaximumRetryAttempts());
        } catch (final SSLHandshakeException e) {
            val sanitizedUrl = FunctionUtils.doUnchecked(
                    () -> new URIBuilder(execution.getUrl()).removeQuery().clearParameters().build().toASCIIString());
            LoggingUtils.error(LOGGER, "SSL error accessing: [" + sanitizedUrl + ']', e);
            return new BasicHttpResponse(org.apache.hc.core5.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, sanitizedUrl);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

}
