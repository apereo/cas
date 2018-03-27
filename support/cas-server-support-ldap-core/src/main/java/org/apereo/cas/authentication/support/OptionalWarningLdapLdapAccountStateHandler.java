package org.apereo.cas.authentication.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.MessageDescriptor;
import org.ldaptive.LdapAttribute;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * The component supports both opt-in and opt-out warnings on a per-user basis.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
@Setter
public class OptionalWarningLdapLdapAccountStateHandler extends DefaultLdapLdapAccountStateHandler {

    private String warnAttributeName;

    private String warningAttributeValue;

    private boolean displayWarningOnMatch;
    
    @Override
    protected void handleWarning(final AccountState.Warning warning, final AuthenticationResponse response,
                                 final LdapPasswordPolicyConfiguration configuration, final List<MessageDescriptor> messages) {
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
