package org.apereo.cas.pm.ldap;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.LdapUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.UUID;

/**
 * This is {@link LdapPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LdapPasswordManagementService implements PasswordManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapPasswordManagementService.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private CipherExecutor<String, String> cipherExecutor;

    public LdapPasswordManagementService(final CipherExecutor<String, String> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public String findEmail(final String username) {
        try {
            final PasswordManagementProperties.Ldap ldap = casProperties.getAuthn().getPm().getLdap();
            final SearchFilter filter = Beans.newSearchFilter(ldap.getUserFilter(), username);
            final ConnectionFactory factory = Beans.newPooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                final LdapAttribute attr = entry.getAttribute(casProperties.getAuthn().getPm().getReset().getEmailAttribute());
                if (attr != null) {
                    final String email = attr.getStringValue();
                    if (EmailValidator.getInstance().isValid(email)) {
                        return email;
                    }
                }
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String createToken(final String to) {
        try {
            final String token = UUID.randomUUID().toString();
            final JwtClaims claims = new JwtClaims();
            claims.setJwtId(token);
            claims.setIssuer(casProperties.getServer().getPrefix());
            claims.setAudience(casProperties.getServer().getPrefix());
            claims.setExpirationTimeMinutesInTheFuture(casProperties.getAuthn().getPm().getReset().getExpirationMinutes());
            claims.setIssuedAtToNow();

            final ClientInfo holder = ClientInfoHolder.getClientInfo();
            claims.setStringClaim("origin", holder.getServerIpAddress());
            claims.setStringClaim("client", holder.getClientIpAddress());

            claims.setSubject(to);
            final String json = claims.toJson();
            return this.cipherExecutor.encode(json);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean change(final Credential credential, final PasswordChangeBean bean) {
        Assert.notNull(credential, "Credential cannot be null");
        Assert.notNull(bean, "PasswordChangeBean cannot be null");

        try {
            final PasswordManagementProperties.Ldap ldap = casProperties.getAuthn().getPm().getLdap();
            final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;

            final SearchFilter filter = Beans.newSearchFilter(ldap.getUserFilter(), c.getId());
            final ConnectionFactory factory = Beans.newPooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory,
                    ldap.getBaseDn(), filter);

            if (LdapUtils.containsResultEntry(response)) {
                final String dn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating account password for {}", dn);
                if (LdapUtils.executePasswordModifyOperation(dn, factory,
                        c.getPassword(), bean.getPassword(),
                        casProperties.getAuthn().getPm().getLdap().getType())) {
                    LOGGER.debug("Successfully updated the account password for {}", dn);
                    return true;
                }
                LOGGER.error("Could not update the LDAP entry's password for {} and base DN {}", filter.format(), ldap.getBaseDn());
            } else {
                LOGGER.error("Could not locate an LDAP entry for {} and base DN {}", filter.format(), ldap.getBaseDn());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String parseToken(final String token) {
        try {
            final String json = this.cipherExecutor.decode(token);
            final JwtClaims claims = JwtClaims.parse(json);

            if (!claims.getIssuer().equals(casProperties.getServer().getPrefix())) {
                LOGGER.error("Token issuer does not match CAS");
                return null;
            }
            if (claims.getAudience().isEmpty() || !claims.getAudience().get(0).equals(casProperties.getServer().getPrefix())) {
                LOGGER.error("Token audience does not match CAS");
                return null;
            }
            if (StringUtils.isBlank(claims.getSubject())) {
                LOGGER.error("Token has no subject identifier");
                return null;
            }

            final ClientInfo holder = ClientInfoHolder.getClientInfo();
            if (!claims.getStringClaimValue("origin").equals(holder.getServerIpAddress())) {
                LOGGER.error("Token origin does not match CAS");
                return null;
            }
            if (!claims.getStringClaimValue("client").equals(holder.getClientIpAddress())) {
                LOGGER.error("Token client does not match CAS");
                return null;
            }

            if (claims.getExpirationTime().isBefore(NumericDate.now())) {
                LOGGER.error("Token has expired.");
                return null;
            }

            return claims.getSubject();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        final Map<String, String> set = Maps.newLinkedHashMap();

        try {
            final PasswordManagementProperties.Ldap ldap = casProperties.getAuthn().getPm().getLdap();
            final SearchFilter filter = Beans.newSearchFilter(ldap.getUserFilter(), username);
            final ConnectionFactory factory = Beans.newPooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                final Map<String, String> qs = casProperties.getAuthn().getPm().getReset().getSecurityQuestionsAttributes();
                qs.forEach((k, v) -> {
                    final LdapAttribute q = entry.getAttribute(k);
                    final LdapAttribute a = entry.getAttribute(v);
                    if (q != null && a != null
                            && StringUtils.isNotBlank(q.getStringValue())
                            && StringUtils.isNotBlank(a.getStringValue())) {
                        set.put(q.getStringValue(), a.getStringValue());
                    }
                });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return set;
    }
}
