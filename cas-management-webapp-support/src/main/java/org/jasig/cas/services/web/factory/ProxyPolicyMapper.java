package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;

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

    /**
     * Map {@link RegisteredServiceProxyPolicy} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param policy the source proxy policy
     * @param bean   the destination data bean
     */
    void mapProxyPolicy(RegisteredServiceProxyPolicy policy, RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceProxyPolicy} represented by the specified {@link ServiceData} bean. Return null
     * if a supported {@link RegisteredServiceProxyPolicy} couldn't be created.
     *
     * @param data a source data bean
     * @return the proxy policy represented by the specified data bean
     */
    RegisteredServiceProxyPolicy toProxyPolicy(ServiceData data);
}
