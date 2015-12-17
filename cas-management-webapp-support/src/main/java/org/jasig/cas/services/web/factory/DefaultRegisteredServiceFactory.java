package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.FormData;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Default implmentation of {@link RegisteredServiceFactory}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@Component
public final class DefaultRegisteredServiceFactory implements RegisteredServiceFactory {
    @NotNull
    @Autowired(required = false)
    private AccessStrategyMapper accessStrategyMapper = new DefaultAccessStrategyMapper();

    @NotNull
    @Autowired(required = false)
    private AttributeFilterMapper attributeFilterMapper = new DefaultAttributeFilterMapper();

    @NotNull
    @Autowired(required = false)
    private AttributeReleasePolicyMapper attributeReleasePolicyMapper;

    @NotNull
    @Autowired(required = false)
    private PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper = new
            DefaultPrincipalAttributesRepositoryMapper();

    @NotNull
    @Autowired(required = false)
    private ProxyPolicyMapper proxyPolicyMapper = new DefaultProxyPolicyMapper();

    @NotNull
    @Autowired(required = false)
    private RegisteredServiceMapper registeredServiceMapper = new DefaultRegisteredServiceMapper();

    @NotNull
    @Autowired(required = false)
    private UsernameAttributeProviderMapper usernameAttributeProviderMapper = new
            DefaultUsernameAttributeProviderMapper();

    @NotNull
    @Autowired
    private List<? extends FormDataPopulator> formDataPopulators;

    public void setAccessStrategyMapper(final AccessStrategyMapper accessStrategyMapper) {
        this.accessStrategyMapper = accessStrategyMapper;
    }

    public void setAttributeFilterMapper(final AttributeFilterMapper attributeFilterMapper) {
        this.attributeFilterMapper = attributeFilterMapper;
    }

    public void setAttributeReleasePolicyMapper(final AttributeReleasePolicyMapper attributeReleasePolicyMapper) {
        this.attributeReleasePolicyMapper = attributeReleasePolicyMapper;
    }

    public void setPrincipalAttributesRepositoryMapper(
            final PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper) {
        this.principalAttributesRepositoryMapper = principalAttributesRepositoryMapper;
    }

    public void setProxyPolicyMapper(final ProxyPolicyMapper proxyPolicyMapper) {
        this.proxyPolicyMapper = proxyPolicyMapper;
    }

    public void setRegisteredServiceMapper(final RegisteredServiceMapper registeredServiceMapper) {
        this.registeredServiceMapper = registeredServiceMapper;
    }

    public void setUsernameAttributeProviderMapper(
            final UsernameAttributeProviderMapper usernameAttributeProviderMapper) {
        this.usernameAttributeProviderMapper = usernameAttributeProviderMapper;
    }

    public void setFormDataPopulators(final List<? extends FormDataPopulator> formDataPopulators) {
        this.formDataPopulators = formDataPopulators;
    }

    /**
     * Initializes some default mappers after any custom mappers have been wired.
     */
    @PostConstruct
    public void initializeDefaults() {
        // initialize default attributeReleasePolicyMapper here to allow switching it's dependencies
        if (attributeReleasePolicyMapper == null) {
            attributeReleasePolicyMapper = new DefaultAttributeReleasePolicyMapper(attributeFilterMapper,
                    principalAttributesRepositoryMapper);
        }
    }

    @Override
    public FormData createFormData() {
        final FormData data = new FormData();
        if (formDataPopulators != null) {
            for (final FormDataPopulator populator : formDataPopulators) {
                populator.populateFormData(data);
            }
        }
        return data;
    }

    @Override
    public ServiceData createServiceData(final RegisteredService svc) {
        final ServiceData bean = new ServiceData();

        registeredServiceMapper.mapRegisteredService(svc, bean);
        accessStrategyMapper.mapAccessStrategy(svc.getAccessStrategy(), bean);
        usernameAttributeProviderMapper.mapUsernameAttributeProvider(svc.getUsernameAttributeProvider(), bean);
        proxyPolicyMapper.mapProxyPolicy(svc.getProxyPolicy(), bean);
        attributeReleasePolicyMapper.mapAttributeReleasePolicy(svc.getAttributeReleasePolicy(), bean);

        return bean;
    }

    @Override
    public RegisteredServiceViewBean createServiceViewBean(final RegisteredService svc) {
        final RegisteredServiceViewBean bean = new RegisteredServiceViewBean();

        registeredServiceMapper.mapRegisteredService(svc, bean);
        accessStrategyMapper.mapAccessStrategy(svc.getAccessStrategy(), bean);
        usernameAttributeProviderMapper.mapUsernameAttributeProvider(svc.getUsernameAttributeProvider(), bean);
        proxyPolicyMapper.mapProxyPolicy(svc.getProxyPolicy(), bean);
        attributeReleasePolicyMapper.mapAttributeReleasePolicy(svc.getAttributeReleasePolicy(), bean);

        return bean;
    }

    @Override
    public RegisteredService createRegisteredService(final ServiceData data) {
        final RegisteredService svc = registeredServiceMapper.toRegisteredService(data);

        if (svc instanceof AbstractRegisteredService) {
            final AbstractRegisteredService absSvc = (AbstractRegisteredService) svc;

            final RegisteredServiceAccessStrategy accessStrategy = accessStrategyMapper.toAccessStrategy(data);
            if (accessStrategy != null) {
                absSvc.setAccessStrategy(accessStrategy);
            }

            final RegisteredServiceUsernameAttributeProvider usernameAttributeProvider =
                    usernameAttributeProviderMapper.toUsernameAttributeProvider(data);
            if (usernameAttributeProvider != null) {
                absSvc.setUsernameAttributeProvider(usernameAttributeProvider);
            }

            final RegisteredServiceProxyPolicy proxyPolicy = proxyPolicyMapper.toProxyPolicy(data);
            if (proxyPolicy != null) {
                absSvc.setProxyPolicy(proxyPolicy);
            }

            final RegisteredServiceAttributeReleasePolicy attrPolicy = attributeReleasePolicyMapper
                    .toAttributeReleasePolicy(data);
            if (attrPolicy != null) {
                absSvc.setAttributeReleasePolicy(attrPolicy);
            }
        }

        return svc;
    }
}
