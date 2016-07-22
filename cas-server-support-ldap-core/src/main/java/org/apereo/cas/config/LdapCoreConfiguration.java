package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.support.DefaultAccountStateHandler;
import org.apereo.cas.authentication.support.LdapPasswordPolicyConfiguration;
import org.apereo.cas.authentication.support.OptionalWarningAccountStateHandler;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapCoreConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public DefaultAccountStateHandler accountStateHandler() {
        final DefaultAccountStateHandler handler = new DefaultAccountStateHandler();
        configureHandler(handler);
        return handler;
    }

    @Bean
    public PasswordPolicyConfiguration ldapPasswordPolicyConfiguration() {
        final LdapPasswordPolicyConfiguration pp =
                new LdapPasswordPolicyConfiguration(this.casProperties.getAuthn().getPasswordPolicy());

        final OptionalWarningAccountStateHandler opt = optionalWarningAccountStateHandler();
        if (StringUtils.isNotBlank(opt.getWarnAttributeName()) && StringUtils.isNotBlank(opt.getWarningAttributeValue())) {
            pp.setAccountStateHandler(opt);
        } else {
            pp.setAccountStateHandler(accountStateHandler());
        }
        return pp;
    }

    @Bean
    public OptionalWarningAccountStateHandler optionalWarningAccountStateHandler() {
        final OptionalWarningAccountStateHandler h = new OptionalWarningAccountStateHandler();

        h.setWarnAttributeName(casProperties.getAuthn().getPasswordPolicy().getWarningAttributeName());
        h.setWarningAttributeValue(casProperties.getAuthn().getPasswordPolicy().getWarningAttributeValue());
        h.setDisplayWarningOnMatch(casProperties.getAuthn().getPasswordPolicy().isDisplayWarningOnMatch());

        configureHandler(h);
        return h;
    }

    private void configureHandler(final DefaultAccountStateHandler handler) {
        handler.setAttributesToErrorMap(casProperties.getAuthn().getPasswordPolicy().getPolicyAttributes());
    }
}
