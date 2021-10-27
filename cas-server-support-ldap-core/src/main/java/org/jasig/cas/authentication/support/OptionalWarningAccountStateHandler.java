package org.jasig.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.MessageDescriptor;
import org.ldaptive.LdapAttribute;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The component supports both opt-in and opt-out warnings on a per-user basis using a simple algorithm of three
 * variables:
 * <ol>
 *     <li>{@link #setWarningAttributeName(String) warningAttributeName}</li>
 *     <li>{@link #setWarningAttributeValue(String)} warningAttributeValue}</li>
 *     <li>{@link #setDisplayWarningOnMatch(boolean) displayWarningOnMatch}</li>
 * </ol>
 * The first two parameters define an attribute on the user entry to match on, and the third parameter determines
 * whether password expiration warnings should be displayed on match.
 * <p>
 * Deployers MUST configure LDAP components to provide {@code warningAttributeName} in the set of attributes
 * returned from the LDAP query for user details.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("optionalWarningAccountStateHandler")
public class OptionalWarningAccountStateHandler extends DefaultAccountStateHandler {

    /** Name of user attribute that describes whether or not to display expiration warnings. */
    @Value("${password.policy.warn.attribute.name:}")
    private String warningAttributeName;

    /** Attribute value to match. */
    @Value("${password.policy.warn.attribute.value:}")
    private String warningAttributeValue;

    /**
     * True to opt into password expiration
     * warnings on match, false to opt out on match.
     **/
    @Value("${password.policy.warn.display.matched:true}")
    private boolean displayWarningOnMatch = true;


    /**
     * Sets the user attribute used to determine whether to display password expiration warnings.
     *
     * @param warningAttributeName Attribute on authenticated user entry.
     */
    public void setWarningAttributeName(final String warningAttributeName) {
        this.warningAttributeName = warningAttributeName;
    }

    /**
     * Sets the value of {@link #warningAttributeName} used as basis of comparison.
     *
     * @param warningAttributeValue Value to match against.
     */
    public void setWarningAttributeValue(final String warningAttributeValue) {
        this.warningAttributeValue = warningAttributeValue;
    }

    /**
     * Determines whether password expiration warnings are opt-in or opt-out.
     *
     * @param displayWarningOnMatch True to opt into password expiration warnings on match, false to opt out on match.
     *                              Default is true.
     */
    public void setDisplayWarningOnMatch(final boolean displayWarningOnMatch) {
        this.displayWarningOnMatch = displayWarningOnMatch;
    }

    @Override
    protected void handleWarning(
            final AccountState.Warning warning,
            final AuthenticationResponse response,
            final LdapPasswordPolicyConfiguration configuration,
            final List<MessageDescriptor> messages) {

        if (StringUtils.isBlank(this.warningAttributeName)) {
            logger.debug("No warning attribute name is defined");
            return;
        }

        if (StringUtils.isBlank(this.warningAttributeValue)) {
            logger.debug("No warning attribute value to match is defined");
            return;
        }


        final LdapAttribute attribute = response.getLdapEntry().getAttribute(this.warningAttributeName);
        boolean matches = false;
        if (attribute != null) {
            logger.debug("Found warning attribute {} with value {}", attribute.getName(), attribute.getStringValue());
            matches = this.warningAttributeValue.equals(attribute.getStringValue());
        }
        logger.debug("matches={}, displayWarningOnMatch={}", matches, displayWarningOnMatch);
        if (displayWarningOnMatch == matches) {
            super.handleWarning(warning, response, configuration, messages);
        }
    }
}
