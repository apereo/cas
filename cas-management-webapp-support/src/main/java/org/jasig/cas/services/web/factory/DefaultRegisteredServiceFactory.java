package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceLogoutTypeEditBean;
import org.jasig.cas.services.web.beans.RegisteredServiceOAuthTypeEditBean;
import org.jasig.cas.services.web.beans.RegisteredServicePublicKeyEditBean;
import org.jasig.cas.services.web.beans.RegisteredServiceTypeEditBean;
import org.jasig.cas.support.oauth.services.OAuthRegisteredCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;

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
    private UsernameAttributeProviderMapper usernameAttributeProviderMapper = new
            DefaultUsernameAttributeProviderMapper();


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

    public void setUsernameAttributeProviderMapper(
            final UsernameAttributeProviderMapper usernameAttributeProviderMapper) {
        this.usernameAttributeProviderMapper = usernameAttributeProviderMapper;
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
    public ServiceData createServiceData(final RegisteredService svc) {
        final ServiceData bean = new ServiceData();

        bean.setAssignedId(svc.getId());
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }
        bean.setRequiredHandlers(svc.getRequiredHandlers());

        accessStrategyMapper.mapAccessStrategy(svc.getAccessStrategy(), bean);

        if (svc instanceof OAuthRegisteredCallbackAuthorizeService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ.toString());
        }

        if (svc instanceof OAuthRegisteredService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH.toString());
            final OAuthRegisteredService oauth = (OAuthRegisteredService) svc;
            final RegisteredServiceOAuthTypeEditBean oauthBean = bean.getOauth();
            oauthBean.setBypass(oauth.isBypassApprovalPrompt());
            oauthBean.setClientId(oauth.getClientId());
            oauthBean.setClientSecret(oauth.getClientSecret());
        }

        bean.setTheme(svc.getTheme());
        bean.setEvalOrder(svc.getEvaluationOrder());
        final LogoutType logoutType = svc.getLogoutType();
        switch (logoutType) {
            case BACK_CHANNEL:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.BACK.toString());
                break;
            case FRONT_CHANNEL:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.FRONT.toString());
                break;
            default:
                bean.setLogoutType(RegisteredServiceLogoutTypeEditBean.NONE.toString());
                break;
        }
        final URL url = svc.getLogoutUrl();
        if (url != null) {
            bean.setLogoutUrl(url.toExternalForm());
        }

        usernameAttributeProviderMapper.mapUsernameAttributeProvider(svc.getUsernameAttributeProvider(), bean);

        final RegisteredServicePublicKey key = svc.getPublicKey();
        final RegisteredServicePublicKeyEditBean pBean = bean.getPublicKey();
        if (key != null) {
            pBean.setAlgorithm(key.getAlgorithm());
            pBean.setLocation(key.getLocation());
        }
        proxyPolicyMapper.mapProxyPolicy(svc.getProxyPolicy(), bean);
        attributeReleasePolicyMapper.mapAttributeReleasePolicy(svc.getAttributeReleasePolicy(), bean);
        return bean;
    }
}
