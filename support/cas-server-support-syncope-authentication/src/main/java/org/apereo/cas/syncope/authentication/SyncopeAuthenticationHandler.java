package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SyncopeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SyncopeAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .findAndRegisterModules();

    private final String syncopeUrl;

    private final String syncopeDomain;

    public SyncopeAuthenticationHandler(final String name,
                                        final ServicesManager servicesManager,
                                        final PrincipalFactory principalFactory,
                                        final String syncopeUrl,
                                        final String syncopeDomain) {
        super(name, servicesManager, principalFactory, null);
        this.syncopeUrl = syncopeUrl;
        this.syncopeDomain = syncopeDomain;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<Object>> buildSyncopeUserAttributes(final JsonNode user) {
        val attributes = new HashMap<String, List<Object>>();

        var roles = MAPPER.convertValue(user.get("roles"), ArrayList.class);
        if (!roles.isEmpty()) {
            attributes.put("syncopeUserRoles", roles);
        }

        if (user.has("securityQuestion")) {
            attributes.put("syncopeUserSecurityQuestion", List.of(user.get("securityQuestion").asText()));
        }

        attributes.put("syncopeUserStatus", List.of(user.get("status").asText()));

        attributes.put("syncopeUserRealm", List.of(user.get("realm").asText()));

        attributes.put("syncopeUserCreator", List.of(user.get("creator").asText()));

        attributes.put("syncopeUserCreationDate", List.of(user.get("creationDate").asText()));

        if (user.has("changePwdDate")) {
            attributes.put("syncopeUserChangePwdDate", List.of(user.get("changePwdDate").asText()));
        }

        if (user.has("lastLoginDate")) {
            attributes.put("syncopeUserLastLoginDate", List.of(user.get("lastLoginDate").asText()));
        }

        var dynRoles = MAPPER.convertValue(user.get("dynRoles"), ArrayList.class);
        if (!dynRoles.isEmpty()) {
            attributes.put("syncopeUserDynRoles", dynRoles);
        }

        var dynRealms = MAPPER.convertValue(user.get("dynRealms"), ArrayList.class);
        if (!dynRealms.isEmpty()) {
            attributes.put("syncopeUserDynRealms", dynRealms);
        }

        var memberships = new ArrayList<>();
        user.get("memberships").forEach(m -> memberships.add(m.get("groupName").asText()));
        if (memberships.isEmpty()) {
            attributes.put("syncopeUserMemberships", memberships);            
        }

        var dynMemberships = new ArrayList<>();
        user.get("dynMemberships").forEach(m -> dynMemberships.add(m.get("groupName").asText()));
        if (dynMemberships.isEmpty()) {
            attributes.put("syncopeUserDynMemberships", dynMemberships);            
        }

        var relationships = new ArrayList<>();
        user.get("dynMemberships").forEach(m -> relationships.add(m.get("groupName").asText()));
        if (relationships.isEmpty()) {
            attributes.put("syncopeUserDynMemberships", relationships);            
        }

        user.get("plainAttrs").forEach(r -> attributes.put(
                "syncopeUserRelationships" + r.get("type").asText(),
                List.of(r.get("otherEndName").asText())));

        user.get("plainAttrs").forEach(a -> attributes.put(
                "syncopeUserAttr" + a.get("schema").asText(),
                MAPPER.convertValue(a.get("values"), ArrayList.class)));

        return attributes;
    }

    @Override
    @SneakyThrows
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) {
        val result = authenticateSyncopeUser(credential);
        if (result.isPresent()) {
            val user = result.get();
            LOGGER.debug("Received user object as [{}]", user);            
            if (user.get("suspended").asBoolean()) {
                throw new AccountDisabledException("Could not authenticate forbidden account for " + credential.getUsername());
            }
            if (user.get("mustChangePassword").asBoolean()) {
                throw new AccountPasswordMustChangeException("Account password must change for " + credential.getUsername());
            }
            val principal = this.principalFactory.createPrincipal(user.get("username").asText(), buildSyncopeUserAttributes(user));
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        }
        throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
    }

    @SneakyThrows
    protected Optional<JsonNode> authenticateSyncopeUser(final UsernamePasswordCredential credential) {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = StringUtils.appendIfMissing(this.syncopeUrl, "/rest/users/self");
            response = Objects.requireNonNull(HttpUtils.executeGet(syncopeRestUrl, credential.getUsername(), credential.getPassword(),
                new HashMap<>(0), CollectionUtils.wrap("X-Syncope-Domain", this.syncopeDomain)));
            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Received user object as [{}]", result);
                return Optional.of(MAPPER.readTree(result));
            }
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
