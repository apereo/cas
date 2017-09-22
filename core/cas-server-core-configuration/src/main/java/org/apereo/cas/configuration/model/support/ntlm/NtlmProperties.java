package org.apereo.cas.configuration.model.support.ntlm;

/**
 * This is {@link NtlmProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class NtlmProperties {
    private String domainController;
    private String includePattern;
    private boolean loadBalance = true;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDomainController() {
        return domainController;
    }

    public void setDomainController(final String domainController) {
        this.domainController = domainController;
    }

    public String getIncludePattern() {
        return includePattern;
    }

    public void setIncludePattern(final String includePattern) {
        this.includePattern = includePattern;
    }

    public boolean isLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(final boolean loadBalance) {
        this.loadBalance = loadBalance;
    }
}
