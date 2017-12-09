package org.apereo.cas.configuration.model.core;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatAjpProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatCsrfProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatExtendedAccessLogProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatHttpProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatHttpProxyProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatRemoteAddressProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatRewriteValveProperties;
import org.apereo.cas.configuration.model.core.web.tomcat.CasEmbeddedApacheTomcatSslValveProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link CasServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core", automated = true)
public class CasServerProperties implements Serializable {
    private static final long serialVersionUID = 7876382696803430817L;

    /**
     * Full name of the CAS server. This is public-facing address
     * of the CAS deployment and not the individual node address,
     * in the event that CAS is clustered.
     */
    @RequiredProperty
    private String name = "https://cas.example.org:8443";
    /**
     * A concatenation of the server name plus the CAS context path.
     * Deployments at root likely need to blank out this value.
     */
    @RequiredProperty
    private String prefix = name.concat("/cas");
    /**
     * Embedded container AJP settings.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatAjpProperties ajp = new CasEmbeddedApacheTomcatAjpProperties();
    /**
     * Embedded container HTTP port settings as an additional option.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatHttpProperties http = new CasEmbeddedApacheTomcatHttpProperties();
    /**
     * Http proxy configuration properties.
     * In the event that you decide to run CAS without any SSL configuration in the embedded Tomcat container and on a non-secure
     * port yet wish to customize the connector configuration that is linked to the running port (i.e. 8080), this setting may apply.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatHttpProxyProperties httpProxy = new CasEmbeddedApacheTomcatHttpProxyProperties();
    /**
     * Embedded container's SSL valve setting.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatSslValveProperties sslValve = new CasEmbeddedApacheTomcatSslValveProperties();
    /**
     * Embedded container's rewrite valve setting.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatRewriteValveProperties rewriteValve = new CasEmbeddedApacheTomcatRewriteValveProperties();
    /**
     * Configuration properties for access logging beyond defaults.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatExtendedAccessLogProperties extAccessLog = new CasEmbeddedApacheTomcatExtendedAccessLogProperties();

    /**
     * Enable Tomcat's RemoteAddress filter.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatRemoteAddressProperties remoteAddr =
        new CasEmbeddedApacheTomcatRemoteAddressProperties();

    /**
     * Enable Tomcat's CSRF filter.
     */
    @NestedConfigurationProperty
    private CasEmbeddedApacheTomcatCsrfProperties csrf = new CasEmbeddedApacheTomcatCsrfProperties();

    public CasEmbeddedApacheTomcatCsrfProperties getCsrf() {
        return csrf;
    }

    public void setCsrf(final CasEmbeddedApacheTomcatCsrfProperties csrf) {
        this.csrf = csrf;
    }

    public CasEmbeddedApacheTomcatRemoteAddressProperties getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(final CasEmbeddedApacheTomcatRemoteAddressProperties remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public CasEmbeddedApacheTomcatHttpProxyProperties getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(final CasEmbeddedApacheTomcatHttpProxyProperties httpProxy) {
        this.httpProxy = httpProxy;
    }

    public CasEmbeddedApacheTomcatExtendedAccessLogProperties getExtAccessLog() {
        return extAccessLog;
    }

    public void setExtAccessLog(final CasEmbeddedApacheTomcatExtendedAccessLogProperties extAccessLog) {
        this.extAccessLog = extAccessLog;
    }

    public CasEmbeddedApacheTomcatHttpProperties getHttp() {
        return http;
    }

    public void setHttp(final CasEmbeddedApacheTomcatHttpProperties http) {
        this.http = http;
    }

    public CasEmbeddedApacheTomcatAjpProperties getAjp() {
        return ajp;
    }

    public void setAjp(final CasEmbeddedApacheTomcatAjpProperties ajp) {
        this.ajp = ajp;
    }

    public CasEmbeddedApacheTomcatSslValveProperties getSslValve() {
        return sslValve;
    }

    public void setSslValve(final CasEmbeddedApacheTomcatSslValveProperties sslValve) {
        this.sslValve = sslValve;
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

    public CasEmbeddedApacheTomcatRewriteValveProperties getRewriteValve() {
        return rewriteValve;
    }

    public void setRewriteValve(final CasEmbeddedApacheTomcatRewriteValveProperties rewriteValve) {
        this.rewriteValve = rewriteValve;
    }
}
