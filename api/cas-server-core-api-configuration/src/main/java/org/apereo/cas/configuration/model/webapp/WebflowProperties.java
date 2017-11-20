package org.apereo.cas.configuration.model.webapp;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * Configuration properties class for webflow.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-webflow")
public class WebflowProperties implements Serializable {

    private static final long serialVersionUID = 4949978905279568311L;
    /**
     * Encryption/signing setting for webflow.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    /**
     * Whether CAS should take control of all spring webflow modifications
     * and dynamically alter views, states and actions.
     */
    private boolean autoconfigure = true;

    /**
     * Whether webflow should remain in "live reload" mode, able to auto detect
     * changes and react. This is useful if the location of the webflow is externalized
     * and changes are done ad-hoc to the webflow to accommodate changes.
     */
    private boolean refresh;

    /**
     * Whether flow executions should redirect after they pause before rendering.
     */
    private boolean alwaysPauseRedirect;

    /**
     * Whether flow executions redirect after they pause for transitions that remain in the same view state.
     */
    private boolean redirectSameState;

    /**
     * Webflow session management settings.
     */
    @NestedConfigurationProperty
    private WebflowSessionManagementProperties session = new WebflowSessionManagementProperties();

    /**
     * Path to groovy resource that may auto-configure the webflow context
     * dynamically creating/removing states and actions.
     */
    private Groovy groovy = new Groovy();

    public Groovy getGroovy() {
        return groovy;
    }

    public void setGroovy(final Groovy groovy) {
        this.groovy = groovy;
    }

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }

    public boolean isAutoconfigure() {
        return autoconfigure;
    }

    public void setAutoconfigure(final boolean autoconfigure) {
        this.autoconfigure = autoconfigure;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(final boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isAlwaysPauseRedirect() {
        return alwaysPauseRedirect;
    }

    public void setAlwaysPauseRedirect(final boolean alwaysPauseRedirect) {
        this.alwaysPauseRedirect = alwaysPauseRedirect;
    }

    public boolean isRedirectSameState() {
        return redirectSameState;
    }

    public void setRedirectSameState(final boolean redirectSameState) {
        this.redirectSameState = redirectSameState;
    }

    public WebflowSessionManagementProperties getSession() {
        return session;
    }

    public void setSession(final WebflowSessionManagementProperties session) {
        this.session = session;
    }

    @RequiresModule(name = "cas-server-core-webflow", automated = true)
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }
}
