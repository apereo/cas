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

import java.net.URL;

/**
 * Default mapper for converting {@link RegisteredService} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultRegisteredServiceMapper implements RegisteredServiceMapper {
    @Override
    public void mapRegisteredService(final RegisteredService svc, final ServiceData bean) {
        bean.setAssignedId(svc.getId());
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }
        bean.setRequiredHandlers(svc.getRequiredHandlers());

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

        final RegisteredServicePublicKey key = svc.getPublicKey();
        final RegisteredServicePublicKeyEditBean pBean = bean.getPublicKey();
        if (key != null) {
            pBean.setAlgorithm(key.getAlgorithm());
            pBean.setLocation(key.getLocation());
        }
    }
}
