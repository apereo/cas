package org.apereo.cas.configuration.model.support.pm;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link PasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
public class PasswordManagementProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -260644582798411176L;

    /**
     * Password management core settings.
     */
    @NestedConfigurationProperty
    private PasswordManagementCoreProperties core = new PasswordManagementCoreProperties();

    /**
     * Google reCAPTCHA settings.
     */
    @NestedConfigurationProperty
    private GoogleRecaptchaProperties googleRecaptcha = new GoogleRecaptchaProperties();

    /**
     * Manage account passwords in LDAP.
     */
    private List<LdapPasswordManagementProperties> ldap = new ArrayList<>();

    /**
     * Manage account passwords in database.
     */
    @NestedConfigurationProperty
    private JdbcPasswordManagementProperties jdbc = new JdbcPasswordManagementProperties();

    /**
     * Manage account passwords via REST.
     */
    @NestedConfigurationProperty
    private RestfulPasswordManagementProperties rest = new RestfulPasswordManagementProperties();

    /**
     * Manage account passwords in JSON resources.
     */
    @NestedConfigurationProperty
    private JsonPasswordManagementProperties json = new JsonPasswordManagementProperties();

    /**
     * Manage account passwords in Syncope.
     */
    @NestedConfigurationProperty
    private SyncopePasswordManagementProperties syncope = new SyncopePasswordManagementProperties();

    /**
     * Settings related to resetting password.
     */
    @NestedConfigurationProperty
    private ResetPasswordManagementProperties reset = new ResetPasswordManagementProperties();

    /**
     * Settings related to fetching usernames.
     */
    @NestedConfigurationProperty
    private ForgotUsernamePasswordManagementProperties forgotUsername =
            new ForgotUsernamePasswordManagementProperties();

    /**
     * Settings related to password history management.
     */
    @NestedConfigurationProperty
    private PasswordHistoryProperties history = new PasswordHistoryProperties();

    /**
     * Handle password policy via Groovy script.
     */
    @NestedConfigurationProperty
    private GroovyPasswordManagementProperties groovy = new GroovyPasswordManagementProperties();

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(200);

    public PasswordManagementCoreProperties getCore() {
        return core;
    }

    public void setCore(PasswordManagementCoreProperties core) {
        this.core = core;
    }

    public GoogleRecaptchaProperties getGoogleRecaptcha() {
        return googleRecaptcha;
    }

    public void setGoogleRecaptcha(GoogleRecaptchaProperties googleRecaptcha) {
        this.googleRecaptcha = googleRecaptcha;
    }

    public List<LdapPasswordManagementProperties> getLdap() {
        return ldap;
    }

    public void setLdap(List<LdapPasswordManagementProperties> ldap) {
        this.ldap = ldap;
    }

    public JdbcPasswordManagementProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcPasswordManagementProperties jdbc) {
        this.jdbc = jdbc;
    }

    public RestfulPasswordManagementProperties getRest() {
        return rest;
    }

    public void setRest(RestfulPasswordManagementProperties rest) {
        this.rest = rest;
    }

    public SyncopePasswordManagementProperties getSyncope() {
        return syncope;
    }

    public void setSyncope(SyncopePasswordManagementProperties syncope) {
        this.syncope = syncope;
    }

    public JsonPasswordManagementProperties getJson() {
        return json;
    }

    public void setJson(JsonPasswordManagementProperties json) {
        this.json = json;
    }

    public ResetPasswordManagementProperties getReset() {
        return reset;
    }

    public void setReset(ResetPasswordManagementProperties reset) {
        this.reset = reset;
    }

    public ForgotUsernamePasswordManagementProperties getForgotUsername() {
        return forgotUsername;
    }

    public void setForgotUsername(
            ForgotUsernamePasswordManagementProperties forgotUsername) {
        this.forgotUsername = forgotUsername;
    }

    public PasswordHistoryProperties getHistory() {
        return history;
    }

    public void setHistory(PasswordHistoryProperties history) {
        this.history = history;
    }

    public GroovyPasswordManagementProperties getGroovy() {
        return groovy;
    }

    public void setGroovy(GroovyPasswordManagementProperties groovy) {
        this.groovy = groovy;
    }

    public WebflowAutoConfigurationProperties getWebflow() {
        return webflow;
    }

    public void setWebflow(WebflowAutoConfigurationProperties webflow) {
        this.webflow = webflow;
    }
}
