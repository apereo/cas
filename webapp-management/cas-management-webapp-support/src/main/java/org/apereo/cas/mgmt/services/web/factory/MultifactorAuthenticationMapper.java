package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

/**
 * This is {@link MultifactorAuthenticationMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationMapper {

    /**
     * Map multifactor policy.
     *
     * @param multifactorPolicy the multifactor policy
     * @param bean              the bean
     */
    void mapMultifactorPolicy(RegisteredServiceMultifactorPolicy multifactorPolicy, RegisteredServiceEditBean.ServiceData bean);

    /**
     * To multifactor policy registered service multifactor policy.
     *
     * @param data the data
     * @return the registered service multifactor policy
     */
    RegisteredServiceMultifactorPolicy toMultifactorPolicy(RegisteredServiceEditBean.ServiceData data);
}
