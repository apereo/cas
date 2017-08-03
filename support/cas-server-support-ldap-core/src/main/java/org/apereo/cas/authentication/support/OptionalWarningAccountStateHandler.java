package org.apereo.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.MessageDescriptor;
import org.ldaptive.LdapAttribute;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The component supports both opt-in and opt-out warnings on a per-user basis.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class OptionalWarningAccountStateHandler extends DefaultAccountStateHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalWarningAccountStateHandler.class);
    
    private String warnAttributeName;
    private String warningAttributeValue;
    private boolean displayWarningOnMatch;

    public String getWarnAttributeName() {
        return warnAttributeName;
    }

    public void setWarnAttributeName(final String warnAttributeName) {
        this.warnAttributeName = warnAttributeName;
    }

    public String getWarningAttributeValue() {
        return warningAttributeValue;
    }

    public void setWarningAttributeValue(final String warningAttributeValue) {
        this.warningAttributeValue = warningAttributeValue;
    }

    public boolean isDisplayWarningOnMatch() {
        return displayWarningOnMatch;
    }

    public void setDisplayWarningOnMatch(final boolean displayWarningOnMatch) {
        this.displayWarningOnMatch = displayWarningOnMatch;
    }

    @Override
    protected void handleWarning(
            final AccountState.Warning warning,
            final AuthenticationResponse response,
            final LdapPasswordPolicyConfiguration configuration,
            final List<MessageDescriptor> messages) {

        if (StringUtils.isBlank(this.warnAttributeName)) {
            LOGGER.debug("No warning attribute name is defined");
            return;
        }

        if (StringUtils.isBlank(this.warningAttributeValue)) {
            LOGGER.debug("No warning attribute value to match is defined");
            return;
        }
        
        final LdapAttribute attribute = response.getLdapEntry().getAttribute(this.warnAttributeName);
        boolean matches = false;
        if (attribute != null) {
            LOGGER.debug("Found warning attribute [{}] with value [{}]", attribute.getName(), attribute.getStringValue());
            matches = this.warningAttributeValue.equals(attribute.getStringValue());
        }
        LOGGER.debug("matches=[{}], displayWarningOnMatch=[{}]", matches, this.displayWarningOnMatch);
        if (this.displayWarningOnMatch == matches) {
            super.handleWarning(warning, response, configuration, messages);
        }
    }
}
