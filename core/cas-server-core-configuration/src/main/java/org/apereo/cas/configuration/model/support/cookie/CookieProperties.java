package org.apereo.cas.configuration.model.support.cookie;

import org.apache.commons.lang3.StringUtils;

/**
 * Common properties for all cookie configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class CookieProperties {

    private String name;
    private String path = StringUtils.EMPTY;
    private String domain = StringUtils.EMPTY;
    private boolean secure = true;
    private boolean httpOnly = true;
    private int maxAge = -1;
    
    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(final int maxAge) {
        this.maxAge = maxAge;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(final boolean secure) {
        this.secure = secure;
    }
}
