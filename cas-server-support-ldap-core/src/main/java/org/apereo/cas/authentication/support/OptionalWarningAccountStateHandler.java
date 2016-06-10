package org.apereo.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.ldaptive.LdapAttribute;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The component supports both opt-in and opt-out warnings on a per-user basis.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class OptionalWarningAccountStateHandler extends DefaultAccountStateHandler {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void handleWarning(
            final AccountState.Warning warning,
            final AuthenticationResponse response,
            final LdapPasswordPolicyConfiguration configuration,
            final List<MessageDescriptor> messages) {

        if (StringUtils.isBlank(casProperties.getAuthn().getPasswordPolicy().getWarningAttributeName())) {
            logger.debug("No warning attribute name is defined");
            return;
        }

        if (StringUtils.isBlank(casProperties.getAuthn().getPasswordPolicy().getWarningAttributeValue())) {
            logger.debug("No warning attribute value to match is defined");
            return;
        }


        final LdapAttribute attribute = response.getLdapEntry().getAttribute(
                casProperties.getAuthn().getPasswordPolicy().getWarningAttributeName());
        boolean matches = false;
        if (attribute != null) {
            logger.debug("Found warning attribute {} with value {}", 
                    attribute.getName(), attribute.getStringValue());
            matches = casProperties.getAuthn().getPasswordPolicy()
                    .getWarningAttributeValue().equals(attribute.getStringValue());
        }
        logger.debug("matches={}, displayWarningOnMatch={}", matches, 
                casProperties.getAuthn().getPasswordPolicy().isDisplayWarningOnMatch());
        if (casProperties.getAuthn().getPasswordPolicy().isDisplayWarningOnMatch() == matches) {
            super.handleWarning(warning, response, configuration, messages);
        }
    }
}
