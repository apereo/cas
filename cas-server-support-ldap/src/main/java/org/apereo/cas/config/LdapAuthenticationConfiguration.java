package org.apereo.cas.config;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.ldap.UnsupportedAuthenticationMechanismException;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.authorization.generator.LdapAuthorizationGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.ldaptive.ad.extended.FastBindOperation;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.SearchEntryResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.provider.Provider;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link LdapAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapAuthenticationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationConfiguration.class);

    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorUserSearchExecutor")
    private SearchExecutor userSearchExecutor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ldapPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration ldapPasswordPolicyConfiguration;

    @Bean
    @RefreshScope
    public AuthorizationGenerator ldapAuthorizationGenerator() {
        if (connectionFactory != null) {
            final LdapAuthorizationGenerator gen =
                    new LdapAuthorizationGenerator(this.connectionFactory, this.userSearchExecutor);
            gen.setAllowMultipleResults(casProperties.getLdapAuthz().isAllowMultipleResults());
            gen.setRoleAttribute(casProperties.getLdapAuthz().getRoleAttribute());
            gen.setRolePrefix(casProperties.getLdapAuthz().getRolePrefix());
            return gen;
        }

        return commonProfile -> {
            throw new UnsupportedAuthenticationMechanismException("Authorization generator not specified");
        };
    }

    @PostConstruct
    public void initLdapAuthenticationHandlers() {
        casProperties.getAuthn().getLdap().forEach(l -> {
            if (l.getType() != null) {
                final LdapAuthenticationHandler handler = new LdapAuthenticationHandler();
                handler.setServicesManager(servicesManager);
                handler.setAdditionalAttributes(l.getAdditionalAttributes());
                handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());

                final Map<String, String> attributes = new HashMap<>();
                l.getPrincipalAttributeList().forEach(a -> attributes.put(a.toString(), a.toString()));
                attributes.putAll(casProperties.getAuthn().getAttributes());
                handler.setPrincipalAttributeMap(attributes);

                handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());

                final Authenticator authenticator = getAuthenticator(l);
                if (l.isUsePasswordPolicy()) {
                    authenticator.setAuthenticationResponseHandlers(
                            new ActiveDirectoryAuthenticationResponseHandler(
                                    TimeUnit.DAYS.convert(this.ldapPasswordPolicyConfiguration.getPasswordWarningNumberOfDays(),
                                            TimeUnit.MILLISECONDS)
                            ),
                            new PasswordPolicyAuthenticationResponseHandler(),
                            new PasswordExpirationAuthenticationResponseHandler());

                    handler.setPasswordPolicyConfiguration(this.ldapPasswordPolicyConfiguration);
                }
                handler.setAuthenticator(authenticator);

                if (l.getAdditionalAttributes().isEmpty() && l.getPrincipalAttributeList().isEmpty()) {
                    this.authenticationHandlersResolvers.put(handler, this.personDirectoryPrincipalResolver);
                } else {
                    this.authenticationHandlersResolvers.put(handler, null);
                }
            }
        });
    }

    private static Authenticator getAuthenticator(final LdapAuthenticationProperties l) {
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.AD) {
            return getActiveDirectoryAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.DIRECT) {
            return getDirectBindAuthenticator(l);
        }
        return getAuthenticatedOrAnonSearchAuthenticator(l);
    }

    private static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final LdapAuthenticationProperties l) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(getPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getDirectBindAuthenticator(final LdapAuthenticationProperties l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getBaseDn());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getActiveDirectoryAuthenticator(final LdapAuthenticationProperties l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l));

        final SearchEntryResolver entryResolver = new SearchEntryResolver();
        entryResolver.setBaseDn(l.getBaseDn());
        entryResolver.setUserFilter(l.getUserFilter());
        entryResolver.setSubtreeSearch(l.isSubtreeSearch());
        authn.setEntryResolver(new SearchEntryResolver());

        return authn;
    }

    private static PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(final LdapAuthenticationProperties l) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(getPooledConnectionFactory(l));
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }

    private static PooledConnectionFactory getPooledConnectionFactory(
            final LdapAuthenticationProperties l) {
        final PoolConfig pc = new PoolConfig();
        pc.setMinPoolSize(l.getMinPoolSize());
        pc.setMaxPoolSize(l.getMaxPoolSize());
        pc.setValidateOnCheckOut(l.isValidateOnCheckout());
        pc.setValidatePeriodically(l.isValidatePeriodically());
        pc.setValidatePeriod(l.getValidatePeriod());

        final ConnectionConfig cc = new ConnectionConfig();
        cc.setLdapUrl(l.getLdapUrl());
        cc.setUseSSL(l.isUseSsl());
        cc.setUseStartTLS(l.isUseStartTls());
        cc.setConnectTimeout(l.getConnectTimeout());

        if (l.getTrustCertificates() != null) {
            final X509CredentialConfig cfg = new X509CredentialConfig();
            cfg.setTrustCertificates(l.getTrustCertificates());
            cc.setSslConfig(new SslConfig());
        } else if (l.getKeystore() != null) {
            final KeyStoreCredentialConfig cfg = new KeyStoreCredentialConfig();
            cfg.setKeyStore(l.getKeystore());
            cfg.setKeyStorePassword(l.getKeystorePassword());
            cfg.setKeyStoreType(l.getKeystoreType());
            cc.setSslConfig(new SslConfig(cfg));
        } else {
            cc.setSslConfig(new SslConfig());
        }

        if (StringUtils.equals(l.getBindCredential(), "*") && StringUtils.equals(l.getBindDn(), "*")) {
            cc.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
        } else if (StringUtils.isNotBlank(l.getBindDn()) && StringUtils.isNotBlank(l.getBindCredential())) {
            cc.setConnectionInitializer(new BindConnectionInitializer(l.getBindDn(),
                    new Credential(l.getBindCredential())));
        }

        final DefaultConnectionFactory bindCf = new DefaultConnectionFactory(cc);

        if (l.getProviderClass() != null) {
            try {
                final Class clazz = ClassUtils.getClass(l.getProviderClass());
                bindCf.setProvider(Provider.class.cast(clazz.newInstance()));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        final BlockingConnectionPool cp = new BlockingConnectionPool(pc, bindCf);

        cp.setBlockWaitTime(l.getBlockWaitTime());
        cp.setPoolConfig(pc);

        final IdlePruneStrategy strategy = new IdlePruneStrategy();
        strategy.setIdleTime(l.getIdleTime());
        strategy.setPrunePeriod(l.getPrunePeriod());

        cp.setPruneStrategy(strategy);
        cp.setValidator(new SearchValidator());
        cp.setFailFastInitialize(l.isFailFast());
        return new PooledConnectionFactory(cp);
    }
}

