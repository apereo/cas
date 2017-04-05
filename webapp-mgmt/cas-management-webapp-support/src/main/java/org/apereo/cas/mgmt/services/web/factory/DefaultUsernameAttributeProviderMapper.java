package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceUsernameAttributeProviderEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegisteredServiceUsernameAttributeProvider;

import java.nio.charset.Charset;

/**
 * Default mapper for converting {@link RegisteredServiceUsernameAttributeProvider} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultUsernameAttributeProviderMapper implements UsernameAttributeProviderMapper {
    @Override
    public void mapUsernameAttributeProvider(final RegisteredServiceUsernameAttributeProvider provider,
                                             final ServiceData bean) {
        final RegisteredServiceUsernameAttributeProviderEditBean uBean = bean.getUserAttrProvider();

        if (provider instanceof DefaultRegisteredServiceUsernameProvider) {
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.DEFAULT.toString());
        } else if (provider instanceof AnonymousRegisteredServiceUsernameAttributeProvider) {
            final AnonymousRegisteredServiceUsernameAttributeProvider anonymous = (AnonymousRegisteredServiceUsernameAttributeProvider) provider;
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.ANONYMOUS.toString());
            final PersistentIdGenerator generator = anonymous.getPersistentIdGenerator();
            if (generator instanceof ShibbolethCompatiblePersistentIdGenerator) {
                final ShibbolethCompatiblePersistentIdGenerator sh = (ShibbolethCompatiblePersistentIdGenerator) generator;

                if (sh.getSalt() != null) {
                    final String salt = new String(sh.getSalt(), Charset.defaultCharset());
                    uBean.setValue(salt);
                } else {
                    throw new IllegalArgumentException("Salt cannot be null");
                }
            }
        } else if (provider instanceof PrincipalAttributeRegisteredServiceUsernameProvider) {
            final PrincipalAttributeRegisteredServiceUsernameProvider p = (PrincipalAttributeRegisteredServiceUsernameProvider) provider;
            uBean.setType(RegisteredServiceUsernameAttributeProviderEditBean.Types.ATTRIBUTE.toString());
            uBean.setValue(p.getUsernameAttribute());
        }
    }

    @Override
    public void mapUsernameAttributeProvider(final RegisteredServiceUsernameAttributeProvider provider,
                                             final RegisteredServiceViewBean bean) {
    }
    
    @Override
    public RegisteredServiceUsernameAttributeProvider toUsernameAttributeProvider(final ServiceData data) {
        final RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider = data.getUserAttrProvider();

        final String uidType = userAttrProvider.getType();
        if (StringUtils.equalsIgnoreCase(uidType, RegisteredServiceUsernameAttributeProviderEditBean.Types.DEFAULT
                .toString())) {
            return new DefaultRegisteredServiceUsernameProvider();
        }
        if (StringUtils.equalsIgnoreCase(uidType, RegisteredServiceUsernameAttributeProviderEditBean.Types
                .ANONYMOUS.toString())) {
            final String salt = userAttrProvider.getValue();
            if (StringUtils.isNotBlank(salt)) {
                final ShibbolethCompatiblePersistentIdGenerator generator = new
                        ShibbolethCompatiblePersistentIdGenerator(salt);
                return new AnonymousRegisteredServiceUsernameAttributeProvider(generator);
            }
            throw new IllegalArgumentException("Invalid sale value for anonymous ids " + salt);
        }
        if (StringUtils.equalsIgnoreCase(uidType, RegisteredServiceUsernameAttributeProviderEditBean.Types
                .ATTRIBUTE.toString())) {
            final String attr = userAttrProvider.getValue();

            if (StringUtils.isNotBlank(attr)) {
                return new PrincipalAttributeRegisteredServiceUsernameProvider(attr);
            }
            throw new IllegalArgumentException("Invalid attribute specified for username");
        }

        return null;
    }
}
