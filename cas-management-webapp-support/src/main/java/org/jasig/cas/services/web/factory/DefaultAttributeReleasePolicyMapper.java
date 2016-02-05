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
import org.jasig.cas.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyViewBean;
import org.jasig.cas.services.web.beans.RegisteredServiceAttributeReleasePolicyViewBean;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Default mapper for converting {@link RegisteredServiceAttributeReleasePolicy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@Component(DefaultAttributeReleasePolicyMapper.BEAN_NAME)
public final class DefaultAttributeReleasePolicyMapper implements AttributeReleasePolicyMapper {
    /**
     * Name of this bean within the Spring context.
     */
    public static final String BEAN_NAME = "defaultAttributeReleasePolicyMapper";

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("attributeFilterMapper")
    private AttributeFilterMapper attributeFilterMapper;

    @Autowired(required = false)
    @Qualifier("principalAttributesRepositoryMapper")
    private PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper;

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
        // use default mappers from spring context
        if (applicationContext != null) {
            if (attributeFilterMapper == null) {
                attributeFilterMapper = applicationContext.getBean(
                        DefaultAttributeFilterMapper.BEAN_NAME,
                        AttributeFilterMapper.class);
            }
            if (principalAttributesRepositoryMapper == null) {
                principalAttributesRepositoryMapper = applicationContext.getBean(
                        DefaultPrincipalAttributesRepositoryMapper.BEAN_NAME,
                        PrincipalAttributesRepositoryMapper.class);
            }
        }

        // initialize default mappers if any are still missing
        if (attributeFilterMapper == null) {
            attributeFilterMapper = new DefaultAttributeFilterMapper();
        }
        if (principalAttributesRepositoryMapper == null) {
            principalAttributesRepositoryMapper = new DefaultPrincipalAttributesRepositoryMapper();
        }
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
    public void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy,
                                          final RegisteredServiceViewBean bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy =
                    (AbstractRegisteredServiceAttributeReleasePolicy) policy;

            final RegisteredServiceAttributeReleasePolicyViewBean attrPolicyBean = bean.getAttrRelease();
            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

            if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.ALL
                        .toString());
            } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
                final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy)
                        attrPolicy;
                if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.NONE
                            .toString());
                } else {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types
                            .ALLOWED.toString());
                }
            } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
                final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy)
                        attrPolicy;
                if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.NONE
                            .toString());
                } else {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.MAPPED
                            .toString());
                }
            }
        }
    }

    @Override
    public RegisteredServiceAttributeReleasePolicy toAttributeReleasePolicy(final ServiceData data) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrRelease = data.getAttrRelease();
        final RegisteredServiceAttributeReleasePolicyStrategyEditBean policyBean = attrRelease.getAttrPolicy();
        final String policyType = policyBean.getType();

        final AbstractRegisteredServiceAttributeReleasePolicy policy;
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
