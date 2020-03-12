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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.RelationshipTO;
import org.apache.syncope.common.lib.to.UserTO;

import javax.security.auth.login.FailedLoginException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link SyncopeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SyncopeAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final ObjectMapper objectMapper = new IgnoringJaxbModuleJacksonObjectMapper().findAndRegisterModules();

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

    private static Map<String, List<Object>> buildSyncopeUserAttributes(final UserTO user) {
        val attributes = new HashMap<String, List<Object>>();

        if (user.getRoles() != null) {
            attributes.put("syncopeUserRoles", (List) user.getRoles());
        }
        if (user.getSecurityQuestion() != null) {
            attributes.put("syncopeUserSecurityQuestion", List.of(user.getSecurityQuestion()));
        }
        attributes.put("syncopeUserStatus", List.of(StringUtils.defaultIfBlank(user.getStatus(), "OK")));
        attributes.put("syncopeUserType", List.of(user.getType()));
        if (user.getRealm() != null) {
            attributes.put("syncopeUserRealm", List.of(user.getRealm()));
        }
        attributes.put("syncopeUserCreator", List.of(StringUtils.defaultIfBlank(user.getCreator(), "NA")));

        if (user.getCreationDate() != null) {
            attributes.put("syncopeUserCreationDate", List.of(user.getCreationDate().toString()));
        }
        val changePwdDate = user.getChangePwdDate();
        if (changePwdDate != null) {
            attributes.put("syncopeUserChangePwdDate", List.of(changePwdDate.toString()));
        }
        val lastLoginDate = user.getLastLoginDate();
        if (lastLoginDate != null) {
            attributes.put("syncopeUserLastLoginDate", List.of(lastLoginDate));
        }
        if (user.getDynRoles() != null && !user.getDynRoles().isEmpty()) {
            attributes.put("syncopeUserDynRoles", (List) user.getDynRoles());
        }
        if (user.getDynRealms() != null && !user.getDynRealms().isEmpty()) {
            attributes.put("syncopeUserDynRealms", (List) user.getDynRealms());
        }
        if (user.getMemberships() != null && !user.getMemberships().isEmpty()) {
            attributes.put("syncopeUserMemberships", user.getMemberships()
                .stream()
                .map(MembershipTO::getGroupName)
                .collect(Collectors.toList()));
        }
        if (user.getMemberships() != null && !user.getMemberships().isEmpty()) {
            attributes.put("syncopeUserDynMemberships", user.getDynMemberships()
                .stream()
                .map(MembershipTO::getGroupName)
                .collect(Collectors.toList()));
        }
        if (user.getRelationships() != null && !user.getRelationships().isEmpty()) {
            attributes.put("syncopeUserRelationships", user.getRelationships()
                .stream()
                .map(RelationshipTO::getType)
                .collect(Collectors.toList()));
        }

        user.getPlainAttrs().forEach(a -> attributes.put("syncopeUserAttr" + a.getSchema(), (List) a.getValues()));
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
            if (user.isSuspended()) {
                throw new AccountDisabledException("Could not authenticate forbidden account for " + credential.getUsername());
            }
            if (user.isMustChangePassword()) {
                throw new AccountPasswordMustChangeException("Account password must change for " + credential.getUsername());
            }
            val principal = this.principalFactory.createPrincipal(user.getUsername(), buildSyncopeUserAttributes(user));
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        }
        throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
    }

    @SneakyThrows
    protected Optional<UserTO> authenticateSyncopeUser(final UsernamePasswordCredential credential) {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = StringUtils.appendIfMissing(this.syncopeUrl, "/rest/users/self");
            response = Objects.requireNonNull(HttpUtils.executeGet(syncopeRestUrl, credential.getUsername(), credential.getPassword(),
                new HashMap<>(0), CollectionUtils.wrap("X-Syncope-Domain", this.syncopeDomain)));
            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Received user object as [{}]", result);
                return Optional.of(this.objectMapper.readValue(result, UserTO.class));
            }
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
