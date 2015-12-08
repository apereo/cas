package org.jasig.cas.services.web.factory;

import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceUsernameAttributeProviderEditBean;

import java.nio.charset.Charset;

/**
 * Default mapper for converting {@link RegisteredServiceUsernameAttributeProvider} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public final class DefaultUsernameAttributeProviderMapper implements UsernameAttributeProviderMapper {
    @Override
    public void mapUsernameAttributeProvider(final RegisteredServiceUsernameAttributeProvider provider,
                                             final ServiceData bean) {
        final RegisteredServiceUsernameAttributeProviderEditBean uBean = bean.getUserAttrProvider();

        if (provider instanceof DefaultRegisteredServiceUsernameProvider) {
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.DEFAULT.toString());
        } else if (provider instanceof AnonymousRegisteredServiceUsernameAttributeProvider) {
            final AnonymousRegisteredServiceUsernameAttributeProvider anonymous =
                    (AnonymousRegisteredServiceUsernameAttributeProvider) provider;
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.ANONYMOUS.toString());
            final PersistentIdGenerator generator = anonymous.getPersistentIdGenerator();
            if (generator instanceof ShibbolethCompatiblePersistentIdGenerator) {
                final ShibbolethCompatiblePersistentIdGenerator sh = (ShibbolethCompatiblePersistentIdGenerator)
                        generator;

                if (sh.getSalt() != null) {
                    final String salt = new String(sh.getSalt(), Charset.defaultCharset());
                    uBean.setValue(salt);
                } else {
                    throw new IllegalArgumentException("Salt cannot be null");
                }
            }
        } else if (provider instanceof PrincipalAttributeRegisteredServiceUsernameProvider) {
            final PrincipalAttributeRegisteredServiceUsernameProvider p =
                    (PrincipalAttributeRegisteredServiceUsernameProvider) provider;
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.ATTRIBUTE.toString());
            uBean.setValue(p.getUsernameAttribute());
        }
    }
}
