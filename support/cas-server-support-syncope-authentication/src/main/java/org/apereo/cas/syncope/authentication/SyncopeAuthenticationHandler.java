package org.apereo.cas.syncope.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.RelationshipTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link SyncopeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SyncopeAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {


    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final String syncopeUrl;
    private final String syncopeDomain;

    public SyncopeAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                        final PrincipalFactory principalFactory, final String syncopeUrl, final String syncopeDomain) {
        super(name, servicesManager, principalFactory, null);
        this.syncopeUrl = syncopeUrl;
        this.syncopeDomain = syncopeDomain;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential c,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        try {
            final String syncopeUrl = StringUtils.appendIfMissing(this.syncopeUrl, "/rest/users/self");
            final HttpResponse response = HttpUtils.executeGet(syncopeUrl, c.getUsername(), c.getPassword(),
                new HashMap<>(), CollectionUtils.wrap("X-Syncope-Domain", this.syncopeDomain));

            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());

            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Received user object as [{}]", result);
                final UserTO user = this.objectMapper.readValue(result, UserTO.class);
                if (user.isSuspended()) {
                    throw new AccountDisabledException("Could not authenticate forbidden account for " + c.getUsername());
                }
                if (user.isMustChangePassword()) {
                    throw new AccountPasswordMustChangeException("Account password must change for " + c.getUsername());
                }
                final Principal principal = this.principalFactory.createPrincipal(user.getUsername(), buildSyncopeUserAttributes(user));
                return createHandlerResult(c, principal, new ArrayList<>());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new FailedLoginException("Could not authenticate account for " + c.getUsername());
    }

    private Map<String, Object> buildSyncopeUserAttributes(final UserTO user) {
        final Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("syncopeUserRoles", user.getRoles());
        if (user.getSecurityQuestion() != null) {
            attributes.put("syncopeUserSecurityQuestion", user.getSecurityQuestion());
        }
        attributes.put("syncopeUserStatus", user.getStatus());
        attributes.put("syncopeUserType", user.getType());
        if (user.getRealm() != null) {
            attributes.put("syncopeUserRealm", user.getRealm());
        }
        attributes.put("syncopeUserCreator", user.getCreator());
        attributes.put("syncopeUserCreationDate", user.getCreationDate().toString());
        if (user.getChangePwdDate() != null) {
            attributes.put("syncopeUserChangePwdDate", user.getChangePwdDate().toString());
        }
        if (user.getLastLoginDate() != null) {
            attributes.put("syncopeUserLastLoginDate", user.getLastLoginDate());
        }
        if (user.getDynRoles() != null && !user.getDynRoles().isEmpty()) {
            attributes.put("syncopeUserDynRoles", user.getDynRoles());
        }
        if (user.getDynRealms() != null && !user.getDynRealms().isEmpty()) {
            attributes.put("syncopeUserDynRealms", user.getDynRealms());
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

        user.getPlainAttrs()
            .stream()
            .forEach(a -> attributes.put("syncopeUserAttr" + a.getSchema(), a.getValues()));
        return attributes;
    }
}
