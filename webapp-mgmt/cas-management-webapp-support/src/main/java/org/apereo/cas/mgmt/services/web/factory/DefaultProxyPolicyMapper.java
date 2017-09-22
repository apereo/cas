package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceProxyPolicyBean;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.util.RegexUtils;

/**
 * Default mapper for converting {@link RegisteredServiceProxyPolicy} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultProxyPolicyMapper implements ProxyPolicyMapper {
    @Override
    public void mapProxyPolicy(final RegisteredServiceProxyPolicy policy, final RegisteredServiceEditBean.ServiceData bean) {
        final RegisteredServiceProxyPolicyBean cBean = bean.getProxyPolicy();
        if (policy == null || policy instanceof RefuseRegisteredServiceProxyPolicy) {
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE);
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy regex = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX);
            cBean.setValue(regex.getPattern().toString());
        }
    }

    @Override
    public void mapProxyPolicy(final RegisteredServiceProxyPolicy policy, final RegisteredServiceViewBean bean) {
        final RegisteredServiceProxyPolicyBean proxyPolicyBean = bean.getProxyPolicy();

        if (policy instanceof RefuseRegisteredServiceProxyPolicy) {
            proxyPolicyBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE);
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy option = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX);
            proxyPolicyBean.setValue(option.getPattern().toString());
        }
    }

    @Override
    public RegisteredServiceProxyPolicy toProxyPolicy(final RegisteredServiceEditBean.ServiceData data) {
        final RegisteredServiceProxyPolicyBean proxyPolicy = data.getProxyPolicy();

        final RegisteredServiceProxyPolicyBean.Types type = proxyPolicy.getType();
        if (type == RegisteredServiceProxyPolicyBean.Types.REGEX) {
            final String value = proxyPolicy.getValue();
            if (StringUtils.isNotBlank(value) && RegexUtils.isValidRegex(value)) {
                return new RegexMatchingRegisteredServiceProxyPolicy(value);
            }
            throw new IllegalArgumentException("Invalid regex pattern specified for proxy policy: " + value);
        }
        if (type == RegisteredServiceProxyPolicyBean.Types.REFUSE) {
            return new RefuseRegisteredServiceProxyPolicy();
        }

        return null;
    }

}
