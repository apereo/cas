package org.apereo.cas.util.junit;

/**
 * This is {@link NoOpCondition}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 * @deprecated Not needed with Junit5
 */
@Deprecated
public class NoOpCondition implements IgnoreCondition {
    @Override
    public Boolean isSatisfied() {
        return null;
    }
}
