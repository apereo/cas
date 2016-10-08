package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mgmt.services.web.beans.AbstractRegisteredServiceAttributeReleasePolicyStrategyBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyViewBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyViewBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Default mapper for converting {@link RegisteredServiceAttributeReleasePolicy} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultAttributeReleasePolicyMapper implements AttributeReleasePolicyMapper {

    private AttributeFilterMapper attributeFilterMapper;

    private PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper;

    public DefaultAttributeReleasePolicyMapper() {
    }

    public DefaultAttributeReleasePolicyMapper(final AttributeFilterMapper attributeFilterMapper,
                                               final PrincipalAttributesRepositoryMapper mapper) {

        this.attributeFilterMapper = attributeFilterMapper;
        this.principalAttributesRepositoryMapper = mapper;
    }

    public void setAttributeFilterMapper(final AttributeFilterMapper attributeFilterMapper) {
        this.attributeFilterMapper = attributeFilterMapper;
    }

    public void setPrincipalAttributesRepositoryMapper(
            final PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper) {
        this.principalAttributesRepositoryMapper = principalAttributesRepositoryMapper;
    }

    /**
     * Initializes some default mappers after any custom mappers have been wired.
     */
    @PostConstruct
    public void initializeDefaults() {
    }

    @Override
    public void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy,
                                          final RegisteredServiceEditBean.ServiceData bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy =
                    (AbstractRegisteredServiceAttributeReleasePolicy) policy;
            final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();

            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

            this.attributeFilterMapper.mapAttributeFilter(attrPolicy.getAttributeFilter(), bean);
            this.principalAttributesRepositoryMapper.mapPrincipalRepository(attrPolicy.getPrincipalAttributesRepository(),
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
            } else if (attrPolicy instanceof DenyAllAttributeReleasePolicy) {
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.DENY.toString());
            }
        }
    }

    @Override
    public void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy,
                                          final RegisteredServiceViewBean bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy =
                    (AbstractRegisteredServiceAttributeReleasePolicy) policy;

            final RegisteredServiceAttributeReleasePolicyViewBean attrPolicyBean = bean.getAttrRelease();
            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

            if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.ALL.toString());
            } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
                final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
                if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.NONE.toString());
                } else {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.ALLOWED.toString());
                }
            } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
                final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
                if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.NONE.toString());
                } else {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.MAPPED.toString());
                }
            } else if (attrPolicy instanceof DenyAllAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.DENY.toString());
            }
        }
    }

    @Override
    public RegisteredServiceAttributeReleasePolicy toAttributeReleasePolicy(final RegisteredServiceEditBean.ServiceData data) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrRelease = data.getAttrRelease();
        final RegisteredServiceAttributeReleasePolicyStrategyEditBean policyBean = attrRelease.getAttrPolicy();
        final String policyType = policyBean.getType();

        final AbstractRegisteredServiceAttributeReleasePolicy policy;
        if (StringUtils.equalsIgnoreCase(policyType, AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALL.toString())) {
            policy = new ReturnAllAttributeReleasePolicy();
        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALLOWED.toString())) {
            policy = new ReturnAllowedAttributeReleasePolicy((List) policyBean.getAttributes());
        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.MAPPED.toString())) {
            policy = new ReturnMappedAttributeReleasePolicy((Map) policyBean.getAttributes());
        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.DENY.toString())) {
            policy = new DenyAllAttributeReleasePolicy();
        } else {
            policy = new ReturnAllowedAttributeReleasePolicy();
        }

        policy.setAuthorizedToReleaseCredentialPassword(attrRelease.isReleasePassword());
        policy.setAuthorizedToReleaseProxyGrantingTicket(attrRelease.isReleaseTicket());

        final RegisteredServiceAttributeFilter filter = this.attributeFilterMapper.toAttributeFilter(data);
        if (filter != null) {
            policy.setAttributeFilter(filter);
        }

        final PrincipalAttributesRepository principalRepository = this.principalAttributesRepositoryMapper
                .toPrincipalRepository(data);
        if (principalRepository != null) {
            policy.setPrincipalAttributesRepository(principalRepository);
        }

        return policy;
    }
}
