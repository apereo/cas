package org.apereo.cas.configuration.model.support.fortress;

/**
 * This is {@link FortressAuthenticationProperties}.
 *
 * @author Yudhi Karunia Surtan
 * @since 5.0.0
 */

public class FortressAuthenticationProperties {

    private String rbaccontext = "HOME";

    public String getRbaccontext() {
        return rbaccontext;
    }

    public void setRbaccontext(final String rbaccontext) {
        this.rbaccontext = rbaccontext;
    }

}
