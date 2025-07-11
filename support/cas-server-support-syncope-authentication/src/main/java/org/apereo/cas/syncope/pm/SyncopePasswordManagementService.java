package org.apereo.cas.syncope.pm;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.syncope.SyncopeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SyncopePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class SyncopePasswordManagementService extends BasePasswordManagementService {
    public SyncopePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                            final CasConfigurationProperties casProperties,
                                            final PasswordHistoryService passwordHistoryService) {
        super(casProperties, cipherExecutor, passwordHistoryService);
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) {
        return false;
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        return getUserAttribute(query, "email").orElseGet(query::getEmail);
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        return getUserAttribute(query, "phoneNumber").orElseGet(query::getEmail);
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        return getUserAttribute(query, "username").orElseGet(query::getEmail);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        throw new UnsupportedOperationException("Password Management Service does not support security questions");
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        throw new UnsupportedOperationException("Password Management Service does not support updating security questions");
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        throw new UnsupportedOperationException("Password Management Service does not support unlocking of accounts");
    }

    protected Optional<String> getUserAttribute(final PasswordManagementQuery query, final String attributeName) {
        return searchUser(query)
            .stream()
            .findFirst()
            .map(syncopeUser -> {
                val prefix = "%s_%s".formatted("syncopeUserAttr", attributeName);
                return syncopeUser.getOrDefault(attributeName, syncopeUser.get(prefix));
            })
            .filter(Objects::nonNull)
            .filter(values -> !values.isEmpty())
            .map(values -> values.getFirst().toString());
    }

    private List<Map<String, List<Object>>> searchUser(final PasswordManagementQuery query) {
        return SyncopeUtils.syncopeUserSearch(casProperties.getAuthn().getPm().getSyncope(), query.getUsername());
    }
}
