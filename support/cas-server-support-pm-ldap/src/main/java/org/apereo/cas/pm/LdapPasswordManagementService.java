package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.LdapPasswordManagementProperties;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.routines.EmailValidator;
import org.jooq.lambda.Unchecked;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.DisposableBean;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final Map<String, ConnectionFactory> connectionFactoryMap;

    public LdapPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final CasConfigurationProperties casProperties,
                                         final PasswordHistoryService passwordHistoryService,
                                         final Map<String, ConnectionFactory> connectionFactoryMap) {
        super(casProperties, cipherExecutor, passwordHistoryService);
        this.connectionFactoryMap = Map.copyOf(connectionFactoryMap);
    }

    @Override
    public void destroy() {
        this.connectionFactoryMap.forEach((ldap, connectionFactory) ->
            connectionFactory.close());
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        val email = findAttribute(query, casProperties.getAuthn().getPm().getReset().getMail().getAttributeName(),
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
        return findAttribute(query, casProperties.getAuthn().getPm().getReset().getSms().getAttributeName(), CollectionUtils.wrap(query.getUsername()));
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        return findAttribute(query, casProperties.getAuthn().getPm().getLdap().stream()
            .map(LdapPasswordManagementProperties::getUsernameAttribute)
            .collect(Collectors.toList()), CollectionUtils.wrap(query.getEmail()));
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        findEntries(CollectionUtils.wrap(query.getUsername()), true)
            .forEach((entry, ldap) -> {
                LOGGER.debug("Located LDAP entry [{}] in the response", entry);
                val questionsAndAnswers = new ArrayDeque<>(ldap.getSecurityQuestionsAttributes().entrySet());
                LOGGER.debug("Security question attributes are defined to be [{}]", questionsAndAnswers);
                val ldapConnectionFactory = new LdapConnectionFactory(connectionFactoryMap.get(ldap.getLdapUrl()));

                val attributes = new LinkedHashMap<String, Set<String>>();
                query.getSecurityQuestions().forEach((question, answers) -> {
                    val attrEntry = questionsAndAnswers.pop();
                    attributes.put(attrEntry.getKey(), Set.of(question));
                    attributes.put(attrEntry.getValue(), Set.copyOf(answers));
                });
                ldapConnectionFactory.executeModifyOperation(entry.getDn(), attributes);
            });
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        val entries = findEntries(CollectionUtils.wrap(credential.getId()), true);
        return entries.entrySet().stream().allMatch(entry -> {
            LOGGER.debug("Located LDAP entry [{}] in the response", entry);
            val ldapConnectionFactory = new LdapConnectionFactory(connectionFactoryMap.get(entry.getValue().getLdapUrl()));
            val attributes = new LinkedHashMap<String, Set<String>>();
            attributes.put(entry.getValue().getAccountLockedAttribute(), Set.of(entry.getValue().getAccountUnlockedAttributeValues()));
            return ldapConnectionFactory.executeModifyOperation(entry.getKey().getDn(), attributes);
        });
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val results = new LinkedHashMap<String, String>();
        findEntries(CollectionUtils.wrap(query.getUsername()), true)
            .forEach((entry, ldap) -> {
                LOGGER.debug("Located LDAP entry [{}] in the response", entry);
                val questionsAndAnswers = ldap.getSecurityQuestionsAttributes();
                LOGGER.debug("Security question attributes are defined to be [{}]", questionsAndAnswers);

                questionsAndAnswers.forEach((key, value) -> {
                    val questionAndAnswer = getSecurityQuestionAndAnswer(entry, ldap, key, value);
                    val question = questionAndAnswer.getKey();
                    val answer = questionAndAnswer.getValue();

                    if (StringUtils.isNotBlank(question) && StringUtils.isNotBlank(answer)) {
                        LOGGER.debug("Added security question [{}] with answer [{}]", question, answer);
                        results.put(question, answer);
                    }
                });
            });
        return results;
    }

    protected Pair<String, String> getSecurityQuestionAndAnswer(
        final LdapEntry entry,
        final LdapPasswordManagementProperties properties,
        final String questionAttributeName,
        final String answerAttributeName) {
        val questionAttribute = entry.getAttribute(questionAttributeName);
        val answerAttribute = entry.getAttribute(answerAttributeName);

        val question = Optional.ofNullable(questionAttribute)
            .map(LdapAttribute::getStringValue)
            .orElse(StringUtils.EMPTY);

        val answer = Optional.ofNullable(answerAttribute)
            .map(LdapAttribute::getStringValue)
            .orElse(StringUtils.EMPTY);

        return Pair.of(question, answer);
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) {
        val results = findEntries(CollectionUtils.wrap(bean.getUsername()), true)
            .entrySet()
            .stream()
            .map(entry -> {
                val dn = entry.getKey().getDn();
                LOGGER.debug("Updating account password for [{}]", dn);
                val ldapConnectionFactory = new LdapConnectionFactory(connectionFactoryMap.get(entry.getValue().getLdapUrl()));
                if (ldapConnectionFactory.executePasswordModifyOperation(dn, bean.getCurrentPassword(),
                    bean.getPassword(), entry.getValue().getType())) {
                    LOGGER.debug("Successfully updated the account password for [{}]", dn);
                    return Boolean.TRUE;
                }
                LOGGER.error("Could not update the LDAP entry's password for [{}]", dn);
                return Boolean.FALSE;
            }).toList();
        return !results.isEmpty() && results.stream().allMatch(BooleanUtils::isTrue);
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
        return findEntries(ldapFilterParam, false)
            .keySet()
            .stream()
            .map(entry -> {
                LOGGER.debug("Found LDAP entry [{}] to use", entry);
                return attributeNames
                    .stream()
                    .map(attributeName -> SpringExpressionLanguageValueResolver.getInstance().resolve(attributeName))
                    .map(attributeName -> {
                        val attr = entry.getAttribute(attributeName);
                        if (attr != null) {
                            val attributeValue = attr.getStringValue();
                            LOGGER.debug("Found [{}] [{}] for user [{}].", attributeName,
                                attributeValue, context.getUsername());
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
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    protected Map<LdapEntry, LdapPasswordManagementProperties> findEntries(
        final List<String> filterValues, final boolean transform) {
        val results = new LinkedHashMap<LdapEntry, LdapPasswordManagementProperties>();
        casProperties.getAuthn().getPm().getLdap()
            .stream()
            .sorted(Comparator.comparing(LdapPasswordManagementProperties::getName))
            .forEach(Unchecked.consumer(ldap -> {
                val effectiveFilters = new ArrayList<>(filterValues);
                if (transform) {
                    val transformer = PrincipalNameTransformerUtils.newPrincipalNameTransformer(ldap.getPrincipalTransformation());
                    effectiveFilters.clear();
                    effectiveFilters.addAll(filterValues.stream().map(Unchecked.function(transformer::transform)).toList());
                }
                val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, effectiveFilters);
                LOGGER.debug("Constructed LDAP filter [{}]", filter);
                val ldapConnectionFactory = new LdapConnectionFactory(connectionFactoryMap.get(ldap.getLdapUrl()));
                val response = ldapConnectionFactory.executeSearchOperation(ldap.getBaseDn(), filter, ldap.getPageSize());
                LOGGER.debug("LDAP response [{}]", response);
                if (LdapUtils.containsResultEntry(response)) {
                    results.put(response.getEntry(), ldap);
                }
            }));
        return results;
    }
}
