package org.apereo.cas.configuration.model.support.gua;

/**
 * This is {@link GraphicalUserAuthenticationProperties}
 * that contains settings needed for identification
 * of users graphically prior to execuring primary authn.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GraphicalUserAuthenticationProperties {
    private String imageAttribute;

    public String getImageAttribute() {
        return imageAttribute;
    }

    public void setImageAttribute(final String imageAttribute) {
        this.imageAttribute = imageAttribute;
    }
}
