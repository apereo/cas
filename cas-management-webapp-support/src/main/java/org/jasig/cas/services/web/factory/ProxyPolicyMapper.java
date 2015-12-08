package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

/**
 * Interface for converting {@link RegisteredServiceProxyPolicy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface ProxyPolicyMapper {
    /**
     * Map {@link RegisteredServiceProxyPolicy} onto the target {@link ServiceData} data bean.
     *
     * @param policy the source proxy policy
     * @param bean   the destination data bean
     */
    void mapProxyPolicy(RegisteredServiceProxyPolicy policy, ServiceData bean);
}
