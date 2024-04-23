package org.apereo.cas.discovery;

/**
 * This is {@link CasServerProfileCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface CasServerProfileCustomizer {
    /**
     * Customize.
     *
     * @param profile the profile
     */
    void customize(CasServerProfile profile);
}
