package org.apereo.cas.discovery;

/**
 * This is {@link CasServerProfileRegistrar}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface CasServerProfileRegistrar {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "casServerProfileRegistrar";
    
    /**
     * Gets profile.
     *
     * @return the profile
     */
    CasServerProfile getProfile();
}
