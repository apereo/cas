package org.apereo.cas.configuration.model.support.fortress;

/**
 * This is {@link FortressProperties}.
 *
 * @author Yudhi Karunia Surtan
 * @since 5.0.0
 */

public class FortressProperties {

    private String rbaccontext = "HOME";
    private String name = "fortressHandler";

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRbaccontext() {
        return rbaccontext;
    }

    public void setRbaccontext(final String rbaccontext) {
        this.rbaccontext = rbaccontext;
    }

}
