package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.util.CollectionUtils;

import org.junit.jupiter.api.Tag;

import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class RadiusMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new RadiusMultifactorAuthenticationProvider(CollectionUtils.wrapList(mock(RadiusServer.class)));
    }
}
