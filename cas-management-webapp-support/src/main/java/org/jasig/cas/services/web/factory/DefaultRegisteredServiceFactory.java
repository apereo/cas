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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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
@Component("registeredServiceFactory")
public final class DefaultRegisteredServiceFactory implements RegisteredServiceFactory {
    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @NotNull
    @Autowired(required = false)
    @Qualifier("accessStrategyMapper")
    private AccessStrategyMapper accessStrategyMapper;

    @NotNull
    @Autowired(required = false)
    @Qualifier("attributeReleasePolicyMapper")
    private AttributeReleasePolicyMapper attributeReleasePolicyMapper;

    @NotNull
    @Autowired(required = false)
    @Qualifier("proxyPolicyMapper")
    private ProxyPolicyMapper proxyPolicyMapper;

    @NotNull
    @Autowired(required = false)
    @Qualifier("registeredServiceMapper")
    private RegisteredServiceMapper registeredServiceMapper;

    @NotNull
    @Autowired(required = false)
    @Qualifier("usernameAttributeProviderMapper")
    private UsernameAttributeProviderMapper usernameAttributeProviderMapper;

    @NotNull
    @Autowired
    private List<? extends FormDataPopulator> formDataPopulators;

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setAccessStrategyMapper(final AccessStrategyMapper accessStrategyMapper) {
        this.accessStrategyMapper = accessStrategyMapper;
    }

    public void setAttributeReleasePolicyMapper(final AttributeReleasePolicyMapper attributeReleasePolicyMapper) {
        this.attributeReleasePolicyMapper = attributeReleasePolicyMapper;
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
        // use default mappers from spring context
        if (applicationContext != null) {
            if (accessStrategyMapper == null) {
                accessStrategyMapper = applicationContext.getBean(
                        DefaultAccessStrategyMapper.BEAN_NAME,
                        AccessStrategyMapper.class);
            }
            if (attributeReleasePolicyMapper == null) {
                attributeReleasePolicyMapper = applicationContext.getBean(
                        DefaultAttributeReleasePolicyMapper.BEAN_NAME,
                        AttributeReleasePolicyMapper.class);
            }
            if (proxyPolicyMapper == null) {
                proxyPolicyMapper = applicationContext.getBean(
                        DefaultProxyPolicyMapper.BEAN_NAME,
                        ProxyPolicyMapper.class);
            }
            if (registeredServiceMapper == null) {
                registeredServiceMapper = applicationContext.getBean(
                        DefaultRegisteredServiceMapper.BEAN_NAME,
                        RegisteredServiceMapper.class);
            }
            if (usernameAttributeProviderMapper == null) {
                usernameAttributeProviderMapper = applicationContext.getBean(
                        DefaultUsernameAttributeProviderMapper.BEAN_NAME,
                        UsernameAttributeProviderMapper.class);
            }
        }

        // initialize default mappers if any are still missing
        if (accessStrategyMapper == null) {
            accessStrategyMapper = new DefaultAccessStrategyMapper();
        }
        if (attributeReleasePolicyMapper == null) {
            final DefaultAttributeReleasePolicyMapper policyMapper = new DefaultAttributeReleasePolicyMapper();
            policyMapper.setApplicationContext(applicationContext);
            policyMapper.initializeDefaults();
            attributeReleasePolicyMapper = policyMapper;
        }
        if (proxyPolicyMapper == null) {
            proxyPolicyMapper = new DefaultProxyPolicyMapper();
        }
        if (registeredServiceMapper == null) {
            registeredServiceMapper = new DefaultRegisteredServiceMapper();
        }
        if (usernameAttributeProviderMapper == null) {
            usernameAttributeProviderMapper = new DefaultUsernameAttributeProviderMapper();
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
