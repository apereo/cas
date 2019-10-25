package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.ldaptive.ConnectionFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link LdapPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class LdapPasswordManagementService extends BasePasswordManagementService {
    private final ConnectionFactory ldapConnectionFactory;

    public LdapPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties,
                                         final PasswordHistoryService passwordHistoryService) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.ldapConnectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(passwordManagementProperties.getLdap());
    }

    @Override
    public String findEmail(final String username) {
        val email = findAttribute(username, properties.getReset().getMail().getAttributeName());
        if (EmailValidator.getInstance().isValid(email)) {
            LOGGER.debug("Email address [{}] for [{}] appears valid", email, username);
            return email;
        }
        LOGGER.warn("Email address [{}] for [{}] is not valid", email, username);
        return null;
    }

    @Override
    public String findPhone(final String username) {
        return findAttribute(username, properties.getReset().getSms().getAttributeName());
    }

    @Override
    public String findUsername(final String email) {
        return findAttribute(email, properties.getLdap().getUsernameAttribute());
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        val set = new HashMap<String, String>();
        try {
            val ldap = properties.getLdap();
            val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                CollectionUtils.wrap(username));
            LOGGER.debug("Constructed LDAP filter [{}] to locate security questions", filter);

            val response = LdapUtils.executeSearchOperation(this.ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
            LOGGER.debug("LDAP response for security questions [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getResult().getEntry();
                LOGGER.debug("Located LDAP entry [{}] in the response", entry);
                val questionsAndAnswers = properties.getLdap().getSecurityQuestionsAttributes();
                LOGGER.debug("Security question attributes are defined to be [{}]", questionsAndAnswers);

                questionsAndAnswers.forEach((k, v) -> {
                    val questionAttribute = entry.getAttribute(k);
                    val answerAttribute = entry.getAttribute(v);

                    val question = questionAttribute.getStringValue();
                    val answer = answerAttribute.getStringValue();

                    if (questionAttribute != null && answerAttribute != null && StringUtils.isNotBlank(question) && StringUtils.isNotBlank(answer)) {
                        LOGGER.debug("Added security question [{}] with answer [{}]", question, answer);
                        set.put(question, answer);
                    }
                });
            } else {
                LOGGER.debug("LDAP response did not contain a result for security questions");
            }
        } catch (final Exception e) {
            LOGGER.error("Error getting security questions: {}", e.getMessage(), e);
        }
        return set;
    }

    /**
     * Perform LDAP search by username, returning the requested attribute.
     *
     * @param username      username for whom an attribute should be found
     * @param attributeName name of the attribute
     * @return String value of attribute; null if user/attribute not present
     */
    private String findAttribute(final String username, final String attributeName) {
        try {
            val ldap = properties.getLdap();
            val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                CollectionUtils.wrap(username));
            LOGGER.debug("Constructed LDAP filter [{}] to locate account [{}]", filter, attributeName);

            val response = LdapUtils.executeSearchOperation(this.ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
            LOGGER.debug("LDAP response to locate [{}] is [{}]", attributeName, response);

            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getResult().getEntry();
                LOGGER.debug("Found LDAP entry [{}] to use for [{}]", entry, attributeName);

                val attr = entry.getAttribute(attributeName);
                if (attr != null) {
                    val attributeValue = attr.getStringValue();
                    LOGGER.debug("Found [{}] [{}] for user [{}].", attributeName, attributeValue, username);
                    return attributeValue;
                } else {
                    LOGGER.warn("Could not locate LDAP attribute [{}] for [{}] and base DN [{}]",
                        attributeName, filter.format(), ldap.getBaseDn());
                }
                return null;
            }
            LOGGER.warn("Could not locate an LDAP entry for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
        } catch (final Exception e) {
            LOGGER.error("Error finding phone: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeRequest bean) {
        try {
            val ldap = properties.getLdap();
            val c = (UsernamePasswordCredential) credential;
            val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                CollectionUtils.wrap(c.getId()));
            LOGGER.debug("Constructed LDAP filter [{}] to update account password", filter);

            val response = LdapUtils.executeSearchOperation(this.ldapConnectionFactory, ldap.getBaseDn(), filter, ldap.getPageSize());
            LOGGER.debug("LDAP response to update password is [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                val dn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating account password for [{}]", dn);
                if (LdapUtils.executePasswordModifyOperation(dn, this.ldapConnectionFactory, c.getPassword(), bean.getPassword(),
                    properties.getLdap().getType())) {
                    LOGGER.debug("Successfully updated the account password for [{}]", dn);
                    return true;
                }
                LOGGER.error("Could not update the LDAP entry's password for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
            } else {
                LOGGER.error("Could not locate an LDAP entry for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
            }
        } catch (final Exception e) {
            LOGGER.error("Error changing password: {}", e.getMessage(), e);
        }
        return false;
    }

}
