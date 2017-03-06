package org.apereo.cas.configuration.model.core;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasServerProperties {

    private Resource rewriteValveConfigLocation = new ClassPathResource("container/tomcat/rewrite.config");
    private String name = "https://cas.example.org:8443";
    private String prefix = name.concat("/cas");
    private Ajp ajp = new Ajp();
    private Http http = new Http();
    private HttpProxy httpProxy = new HttpProxy();
    private ExtendedAccessLog extAccessLog = new ExtendedAccessLog();

    public HttpProxy getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(final HttpProxy httpProxy) {
        this.httpProxy = httpProxy;
    }

    public Resource getRewriteValveConfigLocation() {
        return rewriteValveConfigLocation;
    }

    public void setRewriteValveConfigLocation(final Resource rewriteValveConfigLocation) {
        this.rewriteValveConfigLocation = rewriteValveConfigLocation;
    }

    public ExtendedAccessLog getExtAccessLog() {
        return extAccessLog;
    }

    public void setExtAccessLog(final ExtendedAccessLog extAccessLog) {
        this.extAccessLog = extAccessLog;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(final Http http) {
        this.http = http;
    }

    public Ajp getAjp() {
        return ajp;
    }

    public void setAjp(final Ajp ajp) {
        this.ajp = ajp;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getLoginUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGIN);
    }

    public String getLogoutUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGOUT);
    }

    public static class Ajp {
        private String protocol = "AJP/1.3";
        private int port = 8009;
        private boolean secure;
        private boolean allowTrace;
        private String scheme = "http";
        private boolean enabled;
        private String asyncTimeout = "PT5S";
        private boolean enableLookups;
        private int maxPostSize = 20971520;
        private int proxyPort = -1;
        private int redirectPort = -1;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(final String protocol) {
            this.protocol = protocol;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(final boolean secure) {
            this.secure = secure;
        }

        public boolean isAllowTrace() {
            return allowTrace;
        }

        public void setAllowTrace(final boolean allowTrace) {
            this.allowTrace = allowTrace;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(final String scheme) {
            this.scheme = scheme;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getAsyncTimeout() {
            return Beans.newDuration(asyncTimeout).toMillis();
        }

        public void setAsyncTimeout(final String asyncTimeout) {
            this.asyncTimeout = asyncTimeout;
        }

        public boolean isEnableLookups() {
            return enableLookups;
        }

        public void setEnableLookups(final boolean enableLookups) {
            this.enableLookups = enableLookups;
        }

        public int getMaxPostSize() {
            return maxPostSize;
        }

        public void setMaxPostSize(final int maxPostSize) {
            this.maxPostSize = maxPostSize;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(final int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public int getRedirectPort() {
            return redirectPort;
        }

        public void setRedirectPort(final int redirectPort) {
            this.redirectPort = redirectPort;
        }
    }

    public static class ExtendedAccessLog {
        private boolean enabled;
        private String pattern = "c-ip s-ip cs-uri sc-status time X-threadname x-H(secure) x-H(remoteUser)";
        private String suffix = ".log";
        private String prefix = "localhost_access_extended";
        private String directory;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(final String directory) {
            this.directory = directory;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(final String pattern) {
            this.pattern = pattern;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(final String suffix) {
            this.suffix = suffix;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }
    }

    public static class HttpProxy {
        private boolean enabled = true;
        private String scheme = "https";
        private boolean secure = true;
        private int redirectPort;
        private int proxyPort;

        public int getRedirectPort() {
            return redirectPort;
        }

        public void setRedirectPort(final int redirectPort) {
            this.redirectPort = redirectPort;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(final int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(final String scheme) {
            this.scheme = scheme;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(final boolean secure) {
            this.secure = secure;
        }
    }

    public static class Http {
        private boolean enabled = true;
        private int port = 8080;
        private String protocol = "org.apache.coyote.http11.Http11NioProtocol";

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(final String protocol) {
            this.protocol = protocol;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }
    }
}
