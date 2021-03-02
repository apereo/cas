package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.LdapPasswordManagementProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jooq.lambda.Unchecked;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link LdapPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class LdapPasswordManagementService extends BasePasswordManagementService implements DisposableBean {
    private final List<LdapPasswordManagementProperties> ldapProperties;

    private final Map<String, ConnectionFactory> connectionFactoryMap;

    public LdapPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties,
                                         final PasswordHistoryService passwordHistoryService,
                                         final Map<String, ConnectionFactory> connectionFactoryMap) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.ldapProperties = passwordManagementProperties.getLdap();
        this.connectionFactoryMap = connectionFactoryMap;
    }

    @Override
    public void destroy() {
        this.connectionFactoryMap.forEach((ldap, connectionFactory) ->
            connectionFactory.close());
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        val email = findAttribute(query, List.of(properties.getReset().getMail().getAttributeName()),
            CollectionUtils.wrap(query.getUsername()));
        if (EmailValidator.getInstance().isValid(email)) {
            LOGGER.debug("Email address [{}] for [{}] appears valid", email, query.getUsername());
            return email;
        }
        LOGGER.warn("Email address [{}] for [{}] is not valid", email, query.getUsername());
        return null;
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        return findAttribute(query, List.of(properties.getReset().getSms().getAttributeName()),
            CollectionUtils.wrap(query.getUsername()));
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        return findAttribute(query, properties.getLdap().stream()
            .map(LdapPasswordManagementProperties::getUsernameAttribute)
            .collect(Collectors.toList()), CollectionUtils.wrap(query.getEmail()));
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val results = new HashMap<String, String>(0);
        this.ldapProperties
            .stream()
            .sorted(Comparator.comparing(LdapPasswordManagementProperties::getName))
            .forEach(Unchecked.consumer(ldap -> {
                val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    CollectionUtils.wrap(query.getUsername()));
                LOGGER.debug("Constructed LDAP filter [{}] to locate security questions", filter);
                val ldapConnectionFactory = this.connectionFactoryMap.get(ldap.getLdapUrl());
                val response = LdapUtils.executeSearchOperation(ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
                LOGGER.debug("LDAP response for security questions [{}]", response);

                if (LdapUtils.containsResultEntry(response)) {
                    val entry = response.getEntry();
                    LOGGER.debug("Located LDAP entry [{}] in the response", entry);
                    val questionsAndAnswers = ldap.getSecurityQuestionsAttributes();
                    LOGGER.debug("Security question attributes are defined to be [{}]", questionsAndAnswers);

                    questionsAndAnswers.forEach((k, v) -> {
                        val questionAttribute = entry.getAttribute(k);
                        val answerAttribute = entry.getAttribute(v);
                        if (questionAttribute != null && answerAttribute != null) {
                            val question = questionAttribute.getStringValue();
                            val answer = answerAttribute.getStringValue();
                            if (StringUtils.isNotBlank(question) && StringUtils.isNotBlank(answer)) {
                                LOGGER.debug("Added security question [{}] with answer [{}]", question, answer);
                                results.put(question, answer);
                            }
                        }
                    });
                }
                LOGGER.debug("LDAP response did not contain a result for security questions");
            }));
        return results;
    }

    /**
     * Perform LDAP search by username, returning the requested attribute.
     *
     * @param context         the context
     * @param attributeNames  name of the attribute
     * @param ldapFilterParam the ldap filter param
     * @return String value of attribute; null if user/attribute not present
     */
    protected String findAttribute(final PasswordManagementQuery context,
                                   final List<String> attributeNames,
                                   final List<String> ldapFilterParam) {
        try {
            return this.ldapProperties
                .stream()
                .sorted(Comparator.comparing(LdapPasswordManagementProperties::getName))
                .map(Unchecked.function(ldap -> {
                    val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                        LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, ldapFilterParam);
                    LOGGER.debug("Constructed LDAP filter [{}] to locate account", filter);
                    val ldapConnectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
                    val response = LdapUtils.executeSearchOperation(ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
                    LOGGER.debug("LDAP response is [{}]", response);

                    if (LdapUtils.containsResultEntry(response)) {
                        val entry = response.getEntry();
                        LOGGER.debug("Found LDAP entry [{}] to use", entry);

                        return attributeNames.stream()
                            .map(attributeName -> {
                                val attr = entry.getAttribute(attributeName);
                                if (attr != null) {
                                    val attributeValue = attr.getStringValue();
                                    LOGGER.debug("Found [{}] [{}] for user [{}].", attributeName, attributeValue, context.getUsername());
                                    return attributeValue;
                                }
                                LOGGER.warn("Could not locate LDAP attribute [{}] for [{}] and base DN [{}]",
                                    attributeName, filter.format(), ldap.getBaseDn());
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    }
                    LOGGER.warn("Could not locate an LDAP entry for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
                    return null;
                }))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeRequest bean) {
        try {
            val results = this.ldapProperties
                .stream()
                .sorted(Comparator.comparing(LdapPasswordManagementProperties::getName))
                .map(Unchecked.function(ldap -> {
                    val c = (UsernamePasswordCredential) credential;
                    val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                        LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                        CollectionUtils.wrap(c.getId()));
                    LOGGER.debug("Constructed LDAP filter [{}] to update account password", filter);
                    val ldapConnectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
                    val response = LdapUtils.executeSearchOperation(ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
                    LOGGER.debug("LDAP response to update password is [{}]", response);

                    if (LdapUtils.containsResultEntry(response)) {
                        val dn = response.getEntry().getDn();
                        LOGGER.debug("Updating account password for [{}]", dn);
                        if (LdapUtils.executePasswordModifyOperation(dn, ldapConnectionFactory, c.getPassword(), bean.getPassword(),
                            ldap.getType())) {
                            LOGGER.debug("Successfully updated the account password for [{}]", dn);
                            return Boolean.TRUE;
                        }
                        LOGGER.error("Could not update the LDAP entry's password for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
                    } else {
                        LOGGER.error("Could not locate an LDAP entry for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
                    }
                    return Boolean.FALSE;
                }))
                .collect(Collectors.toList());

            return results.stream().allMatch(result -> result);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

}
