package org.jasig.cas.authentication.principal;

import java.util.Collections;
import java.util.Map;

/**
 * Null principal implementation that allows us to construct {@link org.jasig.cas.authentication.Authentication}s in the event that no
 * principal is resolved during the authentication process.
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class NullPrincipal implements Principal {

    private static final long serialVersionUID = 2309300426720915104L;

    /** The nobody principal. */
    private static final String NOBODY = "nobody";

    /** The singleton instance. **/
    private static NullPrincipal INSTANCE;

    private final Map<String, Object> attributes;

    /**
     * Instantiates a new Null principal.
     */
    private NullPrincipal() {
        attributes = Collections.emptyMap();
    }

    /**
     * Returns the single instance of this class. Will create
     * one if none exists.
     *
     * @return the instance
     */
    public static NullPrincipal getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NullPrincipal();
        }
        return INSTANCE;
    }

    @Override
    public String getId() {
        return NOBODY;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
}
