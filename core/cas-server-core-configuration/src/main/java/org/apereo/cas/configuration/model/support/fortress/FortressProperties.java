package org.apereo.cas.configuration.model.support.fortress;

/**
 * This is {@link FortressProperties}.
 *
 * @author Yudhi Karunia Surtan
 * @since 5.0.0
 */

public class FortressProperties {

    private String rbacContextId = "HOME";
    private String name = "fortressHandler";

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRbacContextId() {
        return rbacContextId;
    }

    public void setRbacContextId(final String rbacContextId) {
        this.rbacContextId = rbacContextId;
    }

}
