package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;

import java.util.List;

/**
 * The component supports both opt-in and opt-out warnings on a per-user basis.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
@Setter
public class OptionalWarningLdapAccountStateHandler extends DefaultLdapAccountStateHandler {

    private String warnAttributeName;

    private String warningAttributeValue;

    private boolean displayWarningOnMatch;

    @Override
    protected void handleWarning(final AccountState.Warning warning, final AuthenticationResponse response,
                                 final PasswordPolicyContext configuration, final List<MessageDescriptor> messages) {
        if (StringUtils.isBlank(this.warnAttributeName)) {
            LOGGER.debug("No warning attribute name is defined");
            return;
        }
        if (StringUtils.isBlank(this.warningAttributeValue)) {
            LOGGER.debug("No warning attribute value to match is defined");
            return;
        }
        val attribute = response.getLdapEntry().getAttribute(this.warnAttributeName);
        var matches = false;
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
