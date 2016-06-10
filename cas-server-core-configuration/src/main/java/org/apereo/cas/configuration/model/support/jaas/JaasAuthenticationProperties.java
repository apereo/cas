package org.apereo.cas.configuration.model.support.jaas;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link JaasAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class JaasAuthenticationProperties {
    private String realm = "CAS";
    private String kerberosRealmSystemProperty;
    private String kerberosKdcSystemProperty;

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public String getKerberosRealmSystemProperty() {
        return kerberosRealmSystemProperty;
    }

    public void setKerberosRealmSystemProperty(final String kerberosRealmSystemProperty) {
        this.kerberosRealmSystemProperty = kerberosRealmSystemProperty;
    }

    public String getKerberosKdcSystemProperty() {
        return kerberosKdcSystemProperty;
    }

    public void setKerberosKdcSystemProperty(final String kerberosKdcSystemProperty) {
        this.kerberosKdcSystemProperty = kerberosKdcSystemProperty;
    }
}
