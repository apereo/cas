package org.jasig.cas.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.jasig.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ReturnMappedAttributeReleasePolicy;
import org.jasig.cas.services.web.beans.AbstractRegisteredServiceAttributeReleasePolicyStrategyBean;
import org.jasig.cas.services.web.beans.RegisteredServiceAttributeReleasePolicyEditBean;
import org.jasig.cas.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyEditBean;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

import java.util.List;
import java.util.Map;

/**
 * Default mapper for converting {@link RegisteredServiceAttributeReleasePolicy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public final class DefaultAttributeReleasePolicyMapper implements AttributeReleasePolicyMapper {
    private final AttributeFilterMapper attributeFilterMapper;

    private final PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper;

    /**
     * Default constructor.
     */
    public DefaultAttributeReleasePolicyMapper() {
        this(new DefaultAttributeFilterMapper(), new DefaultPrincipalAttributesRepositoryMapper());
    }

    /**
     * Build this mapper with a custom {@link AttributeFilterMapper} and {@link PrincipalAttributesRepositoryMapper}.
     *
     * @param attributeFilterMapper     The mapper to use for mapping {@link RegisteredServiceAttributeFilter}
     * @param principalAttributesMapper The mapper to use for mapping {@link PrincipalAttributesRepository}
     */
    public DefaultAttributeReleasePolicyMapper(final AttributeFilterMapper attributeFilterMapper,
                                               final PrincipalAttributesRepositoryMapper principalAttributesMapper) {
        this.attributeFilterMapper = attributeFilterMapper;
        this.principalAttributesRepositoryMapper = principalAttributesMapper;
    }

    @Override
    public void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy,
                                          final ServiceData bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy =
                    (AbstractRegisteredServiceAttributeReleasePolicy) policy;
            final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();

            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

            attributeFilterMapper.mapAttributeFilter(attrPolicy.getAttributeFilter(), bean);
            principalAttributesRepositoryMapper.mapPrincipalRepository(attrPolicy.getPrincipalAttributesRepository(),
                    bean);

            final RegisteredServiceAttributeReleasePolicyStrategyEditBean sBean = attrPolicyBean.getAttrPolicy();
            if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALL.toString());
            } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
                final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy)
                        attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALLOWED.toString());
                sBean.setAttributes(attrPolicyAllowed.getAllowedAttributes());
            } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
                final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy)
                        attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.MAPPED.toString());
                sBean.setAttributes(attrPolicyAllowed.getAllowedAttributes());
            }
        }
    }

    @Override
    public RegisteredServiceAttributeReleasePolicy toAttributeReleasePolicy(final ServiceData data) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrRelease = data.getAttrRelease();
        final RegisteredServiceAttributeReleasePolicyStrategyEditBean policyBean = attrRelease.getAttrPolicy();
        final String policyType = policyBean.getType();

        AbstractRegisteredServiceAttributeReleasePolicy policy;
        if (StringUtils.equalsIgnoreCase(policyType, AbstractRegisteredServiceAttributeReleasePolicyStrategyBean
                .Types.ALL.toString())) {
            policy = new ReturnAllAttributeReleasePolicy();
        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALLOWED.toString())) {
            policy = new ReturnAllowedAttributeReleasePolicy((List) policyBean.getAttributes());
        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.MAPPED.toString())) {
            policy = new ReturnMappedAttributeReleasePolicy((Map) policyBean.getAttributes());
        } else {
            policy = new ReturnAllowedAttributeReleasePolicy();
        }

        policy.setAuthorizedToReleaseCredentialPassword(attrRelease.isReleasePassword());
        policy.setAuthorizedToReleaseProxyGrantingTicket(attrRelease.isReleaseTicket());

        final RegisteredServiceAttributeFilter filter = attributeFilterMapper.toAttributeFilter(data);
        if (filter != null) {
            policy.setAttributeFilter(filter);
        }

        final PrincipalAttributesRepository principalRepository = principalAttributesRepositoryMapper
                .toPrincipalRepository(data);
        if (principalRepository != null) {
            policy.setPrincipalAttributesRepository(principalRepository);
        }

        return policy;
    }
}
