package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines a JSON bean that is mapped
 * to the CAS service access strategy.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceSupportAccessEditBean implements Serializable {

    private static final long serialVersionUID = 2995938566845586064L;

    /**
     * The enum Types.
     */
    public enum Types {
        /** Default type. */
        DEFAULT,
        /** Time based access. */
        TIME,
        /** Grouper-based access. */
        GROUPER,
        /** Remote endpoint access. */
        REMOTE
    }
    
    private String startingTime;
    private String endingTime;
    private boolean casEnabled;
    private boolean ssoEnabled;
    private boolean requireAll;
    private String unauthorizedRedirectUrl;
    private Types type;
    private String groupField;
    private String codes;
    private String url;
    
    private Map<String, Set<String>> requiredAttr = new HashMap<>();
    private Set<RegisteredServiceEditBean.ServiceData.PropertyBean> rejectedAttr = new LinkedHashSet<>();
    private boolean caseSensitive;

    public boolean isCasEnabled() {
        return this.casEnabled;
    }

    public void setCasEnabled(final boolean casEnabled) {
        this.casEnabled = casEnabled;
    }

    public boolean isSsoEnabled() {
        return this.ssoEnabled;
    }

    public void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public boolean isRequireAll() {
        return this.requireAll;
    }

    public void setRequireAll(final boolean requireAll) {
        this.requireAll = requireAll;
    }

    public Map<String, Set<String>> getRequiredAttr() {
        return this.requiredAttr;
    }

    public void setRequiredAttr(final Map<String, Set<String>> requiredAttr) {
        this.requiredAttr = requiredAttr;
    }

    public void setRejectedAttr(final Set<RegisteredServiceEditBean.ServiceData.PropertyBean> rejectedAttr) {
        this.rejectedAttr = rejectedAttr;
    }

    public Set<RegisteredServiceEditBean.ServiceData.PropertyBean> getRejectedAttr() {
        return rejectedAttr;
    }

    public String getStartingTime() {
        return this.startingTime;
    }

    public void setStartingTime(final String startingTime) {
        this.startingTime = startingTime;
    }

    public String getEndingTime() {
        return this.endingTime;
    }

    public void setEndingTime(final String endingTime) {
        this.endingTime = endingTime;
    }

    public String getUnauthorizedRedirectUrl() {
        return unauthorizedRedirectUrl;
    }

    public void setUnauthorizedRedirectUrl(final String unauthorizedRedirectUrl) {
        this.unauthorizedRedirectUrl = unauthorizedRedirectUrl;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public Types getType() {
        return type;
    }

    public void setType(final Types type) {
        this.type = type;
    }

    public String getGroupField() {
        return groupField;
    }

    public void setGroupField(final String groupField) {
        this.groupField = groupField;
    }

    public String getCodes() {
        return codes;
    }

    public void setCodes(final String codes) {
        this.codes = codes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
    
    
}
