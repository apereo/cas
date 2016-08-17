package org.apereo.cas.configuration.model.support.pm;

/**
 * This is {@link PasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordManagementProperties {
    private boolean enabled;
    
    // Minimum 8 and Maximum 10 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character
    private String policyPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,10}";
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getPolicyPattern() {
        return policyPattern;
    }

    public void setPolicyPattern(final String policyPattern) {
        this.policyPattern = policyPattern;
    }
}
