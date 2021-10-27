package org.jasig.cas.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceProxyPolicyBean;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;
import org.jasig.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default mapper for converting {@link RegisteredServiceProxyPolicy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@Component(DefaultProxyPolicyMapper.BEAN_NAME)
public final class DefaultProxyPolicyMapper implements ProxyPolicyMapper {
    /**
     * Name of this bean within the Spring context.
     */
    public static final String BEAN_NAME = "defaultProxyPolicyMapper";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProxyPolicyMapper.class);

    @Override
    public void mapProxyPolicy(final RegisteredServiceProxyPolicy policy, final ServiceData bean) {
        final RegisteredServiceProxyPolicyBean cBean = bean.getProxyPolicy();
        if (policy == null || policy instanceof RefuseRegisteredServiceProxyPolicy) {
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy regex = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX.toString());
            cBean.setValue(regex.getPattern().toString());
        }
    }

    @Override
    public void mapProxyPolicy(final RegisteredServiceProxyPolicy policy, final RegisteredServiceViewBean bean) {
        final RegisteredServiceProxyPolicyBean proxyPolicyBean = bean.getProxyPolicy();

        if (policy instanceof RefuseRegisteredServiceProxyPolicy) {
            proxyPolicyBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy option = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX.toString());
            proxyPolicyBean.setValue(option.getPattern().toString());
        }
    }

    @Override
    public RegisteredServiceProxyPolicy toProxyPolicy(final ServiceData data) {
        final RegisteredServiceProxyPolicyBean proxyPolicy = data.getProxyPolicy();

        final String type = proxyPolicy.getType();
        if (StringUtils.equalsIgnoreCase(type, RegisteredServiceProxyPolicyBean.Types.REGEX.toString())) {
            final String value = proxyPolicy.getValue();
            if (StringUtils.isNotBlank(value) && RegexUtils.isValidRegex(value)) {
                return new RegexMatchingRegisteredServiceProxyPolicy(value);
            } else {
                throw new IllegalArgumentException("Invalid regex pattern specified for proxy policy: " + value);
            }
        } else if (StringUtils.equalsIgnoreCase(type, RegisteredServiceProxyPolicyBean.Types.REFUSE.toString())) {
            return new RefuseRegisteredServiceProxyPolicy();
        }

        return null;
    }

}
