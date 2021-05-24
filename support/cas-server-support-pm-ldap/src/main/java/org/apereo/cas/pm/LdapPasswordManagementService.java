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
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        findEntries(CollectionUtils.wrap(query.getUsername()))
            .forEach((entry, ldap) -> {
                LOGGER.debug("Located LDAP entry [{}] in the response", entry);
                val questionsAndAnswers = new ArrayDeque<>(ldap.getSecurityQuestionsAttributes().entrySet());
                LOGGER.debug("Security question attributes are defined to be [{}]", questionsAndAnswers);
                val ldapConnectionFactory = this.connectionFactoryMap.get(ldap.getLdapUrl());

                val attributes = new LinkedHashMap<String, Set<String>>();
                query.getSecurityQuestions().forEach((question, answers) -> {
                    val attrEntry = questionsAndAnswers.pop();
                    attributes.put(attrEntry.getKey(), Set.of(question));
                    attributes.put(attrEntry.getValue(), Set.copyOf(answers));
                });
                LdapUtils.executeModifyOperation(entry.getDn(), ldapConnectionFactory, attributes);
            });
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val results = new LinkedHashMap<String, String>(0);
        findEntries(CollectionUtils.wrap(query.getUsername()))
            .forEach((entry, ldap) -> {
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
            });
        return results;
    }

    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeRequest bean) {
        try {
            val results = findEntries(CollectionUtils.wrap(credential.getId()))
                .entrySet()
                .stream()
                .map(entry -> {
                    val dn = entry.getKey().getDn();
                    LOGGER.debug("Updating account password for [{}]", dn);
                    val c = (UsernamePasswordCredential) credential;
                    val ldapConnectionFactory = this.connectionFactoryMap.get(entry.getValue().getLdapUrl());
                    if (LdapUtils.executePasswordModifyOperation(dn, ldapConnectionFactory, c.getPassword(), bean.getPassword(),
                        entry.getValue().getType())) {
                        LOGGER.debug("Successfully updated the account password for [{}]", dn);
                        return Boolean.TRUE;
                    }
                    LOGGER.error("Could not update the LDAP entry's password for [{}]", dn);
                    return Boolean.FALSE;
                })
                .collect(Collectors.toList());
            return results.stream().allMatch(result -> result);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
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
        return findEntries(ldapFilterParam)
            .keySet()
            .stream()
            .map(entry -> {
                LOGGER.debug("Found LDAP entry [{}] to use", entry);
                return attributeNames.stream()
                    .map(attributeName -> {
                        val attr = entry.getAttribute(attributeName);
                        if (attr != null) {
                            val attributeValue = attr.getStringValue();
                            LOGGER.debug("Found [{}] [{}] for user [{}].", attributeName, attributeValue, context.getUsername());
                            return attributeValue;
                        }
                        LOGGER.warn("Could not locate LDAP attribute [{}] for [{}]",
                            attributeName, entry.getDn());
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            })
            .findFirst()
            .orElse(null);
    }

    private Map<LdapEntry, LdapPasswordManagementProperties> findEntries(final List<String> filterValues) {
        val results = new LinkedHashMap<LdapEntry, LdapPasswordManagementProperties>(0);
        ldapProperties
            .stream()
            .sorted(Comparator.comparing(LdapPasswordManagementProperties::getName))
            .forEach(Unchecked.consumer(ldap -> {
                val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, filterValues);
                LOGGER.debug("Constructed LDAP filter [{}]", filter);
                val ldapConnectionFactory = this.connectionFactoryMap.get(ldap.getLdapUrl());
                val response = LdapUtils.executeSearchOperation(ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
                LOGGER.debug("LDAP response [{}]", response);

                if (LdapUtils.containsResultEntry(response)) {
                    results.put(response.getEntry(), ldap);
                }
            }));
        return results;
    }
}
