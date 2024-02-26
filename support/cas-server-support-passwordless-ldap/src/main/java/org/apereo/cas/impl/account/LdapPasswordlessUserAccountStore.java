package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationLdapAccountsProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This is {@link LdapPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class LdapPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private final LdapConnectionFactory connectionFactory;

    private final PasswordlessAuthenticationLdapAccountsProperties ldapProperties;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        try {
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                CollectionUtils.wrap(request.getUsername()));

            LOGGER.debug("Constructed LDAP filter [{}] to locate passwordless account", filter);
            val response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize());
            LOGGER.debug("LDAP response for passwordless account is [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getEntry();
                val acctBuilder = PasswordlessUserAccount
                    .builder()
                    .username(request.getUsername())
                    .name(request.getUsername());

                setAttribute(entry, ldapProperties::getUsernameAttribute, acctBuilder::username);
                setAttribute(entry, ldapProperties::getNameAttribute, acctBuilder::name);
                setAttribute(entry, ldapProperties::getEmailAttribute, acctBuilder::email);
                setAttribute(entry, ldapProperties::getPhoneAttribute, acctBuilder::phone);
                setAttribute(entry, ldapProperties::getRequestPasswordAttribute,
                    value -> acctBuilder.requestPassword(BooleanUtils.toBoolean(value)));

                val attributes = entry.getAttributes()
                    .stream()
                    .collect(Collectors.toMap(LdapAttribute::getName,
                        attr -> new ArrayList<>(attr.getStringValues()), (__, b) -> b,
                        () -> new LinkedHashMap<String, List<String>>(entry.getAttributes().size())));

                val acct = acctBuilder.attributes(attributes).build();
                LOGGER.debug("Final passwordless account is [{}]", acct);

                if (StringUtils.isNotBlank(ldapProperties.getRequiredAttribute())
                    && StringUtils.isNotBlank(ldapProperties.getRequiredAttributeValue())) {
                    val attributeValues = acct.getAttributes().getOrDefault(ldapProperties.getRequiredAttribute(), List.of());
                    if (attributeValues.stream().noneMatch(value -> RegexUtils.find(ldapProperties.getRequiredAttributeValue(), value))) {
                        LOGGER.warn("Passwordless account [{}] does not have the required attribute [{}] with value pattern [{}]",
                            acct, ldapProperties.getRequiredAttribute(), ldapProperties.getRequiredAttributeValue());
                        return Optional.empty();
                    }
                }
                return Optional.of(acct);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }

    protected void setAttribute(final LdapEntry entry, final Supplier<String> attribute,
                                final Consumer<String> attributeSetter) {
        val attributeName = attribute.get();
        if (entry.getAttribute(attributeName) != null) {
            val attributeValue = entry.getAttribute(attributeName).getStringValue();
            attributeSetter.accept(attributeValue);
        }
    }
}
