package org.jasig.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceLogoutTypeEditBean;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceOAuthTypeEditBean;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServicePublicKeyEditBean;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceTypeEditBean;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServicePublicKey;
import org.jasig.cas.services.RegisteredServicePublicKeyImpl;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URL;

import static org.slf4j.LoggerFactory.*;

/**
 * Default mapper for converting {@link RegisteredService} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@Component(DefaultRegisteredServiceMapper.BEAN_NAME)
public class DefaultRegisteredServiceMapper implements RegisteredServiceMapper {
    /**
     * Name of this bean within the Spring context.
     */
    public static final String BEAN_NAME = "defaultRegisteredServiceMapper";

    private static final Logger LOGGER = getLogger(DefaultRegisteredServiceMapper.class);

    @Override
    public void mapRegisteredService(final RegisteredService svc, final ServiceData bean) {
        bean.setAssignedId(Long.toString(svc.getId()));
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }
        bean.setRequiredHandlers(svc.getRequiredHandlers());

        if (svc instanceof OAuthCallbackAuthorizeService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ.toString());
        }

        if (svc instanceof OAuthRegisteredService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH.toString());
            final OAuthRegisteredService oauth = (OAuthRegisteredService) svc;
            final RegisteredServiceOAuthTypeEditBean oauthBean = bean.getOauth();
            oauthBean.setBypass(oauth.isBypassApprovalPrompt());
            oauthBean.setClientId(oauth.getClientId());
            oauthBean.setClientSecret(oauth.getClientSecret());
            oauthBean.setRefreshToken(oauth.isGenerateRefreshToken());
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

    @Override
    public void mapRegisteredService(final RegisteredService svc, final RegisteredServiceViewBean bean) {
        bean.setAssignedId(Long.toString(svc.getId()));
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        bean.setEvalOrder(svc.getEvaluationOrder());

        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }
    }

    @Override
    public RegisteredService toRegisteredService(final ServiceData data) {
        try {
            final AbstractRegisteredService regSvc;

            // create base RegisteredService object
            final String type = data.getType();
            if (StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.OAUTH_CALLBACK_AUTHZ.toString())) {
                regSvc = new OAuthCallbackAuthorizeService();
            } else if (StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.OAUTH.toString())) {
                regSvc = new OAuthRegisteredService();

                final RegisteredServiceOAuthTypeEditBean oauthBean = data.getOauth();
                ((OAuthRegisteredService) regSvc).setClientId(oauthBean.getClientId());
                ((OAuthRegisteredService) regSvc).setClientSecret(oauthBean.getClientSecret());
                ((OAuthRegisteredService) regSvc).setBypassApprovalPrompt(oauthBean.isBypass());
                ((OAuthRegisteredService) regSvc).setGenerateRefreshToken(oauthBean.isRefreshToken());
            } else {
                if (RegexUtils.isValidRegex(data.getServiceId())) {
                    regSvc = new RegexRegisteredService();
                } else {
                    throw new RuntimeException("Invalid service type.");
                }
            }

            // set the assigned Id
            final long assignedId = Long.parseLong(data.getAssignedId());
            if (assignedId <= 0) {
                regSvc.setId(RegisteredService.INITIAL_IDENTIFIER_VALUE);
            } else {
                regSvc.setId(assignedId);
            }

            // set simple RegisteredService properties
            regSvc.setServiceId(data.getServiceId());
            regSvc.setName(data.getName());
            regSvc.setDescription(data.getDescription());

            if (StringUtils.isNotBlank(data.getLogoUrl())) {
                regSvc.setLogo(new URL(data.getLogoUrl()));
            }
            regSvc.setTheme(data.getTheme());
            regSvc.setEvaluationOrder(data.getEvalOrder());
            regSvc.setRequiredHandlers(data.getRequiredHandlers());

            // process logout settings
            parseLogoutType(data.getLogoutType());
            if (StringUtils.isNotBlank(data.getLogoutUrl())) {
                regSvc.setLogoutUrl(new URL(data.getLogoutUrl()));
            }

            // process the Public Key
            final RegisteredServicePublicKeyEditBean publicKey = data.getPublicKey();
            if (publicKey != null && publicKey.isValid()) {
                regSvc.setPublicKey(new RegisteredServicePublicKeyImpl(publicKey.getLocation(), publicKey
                        .getAlgorithm()));
            }

            return regSvc;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Parse raw logout type string to {@link LogoutType}.
     *
     * @param logoutType the reg svc
     */
    private LogoutType parseLogoutType(final String logoutType) {
        if (StringUtils.equalsIgnoreCase(logoutType, RegisteredServiceLogoutTypeEditBean.BACK.toString())) {
            return LogoutType.BACK_CHANNEL;
        } else if (StringUtils.equalsIgnoreCase(logoutType, RegisteredServiceLogoutTypeEditBean.FRONT.toString())) {
            return LogoutType.FRONT_CHANNEL;
        } else {
            return LogoutType.NONE;
        }
    }
}
