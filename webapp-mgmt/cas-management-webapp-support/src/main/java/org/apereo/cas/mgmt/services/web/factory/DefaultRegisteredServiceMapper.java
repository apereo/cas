package org.apereo.cas.mgmt.services.web.factory;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceLogoutTypeEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceOAuthTypeEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceOidcTypeEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServicePublicKeyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceSamlTypeEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceTypeEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.LogoutType;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServicePublicKey;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RegexUtils;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default mapper for converting {@link RegisteredService} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultRegisteredServiceMapper implements RegisteredServiceMapper {
    @Override
    public void mapRegisteredService(final RegisteredService svc, final RegisteredServiceEditBean.ServiceData bean) {
        bean.setAssignedId(Long.toString(svc.getId()));
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }
        bean.setRequiredHandlers(svc.getRequiredHandlers());

        if (StringUtils.isNotBlank(svc.getInformationUrl())) {
            bean.setInformationUrl(svc.getInformationUrl());
        }
        if (StringUtils.isNotBlank(svc.getPrivacyUrl())) {
            bean.setPrivacyUrl(svc.getPrivacyUrl());
        }

        if (svc instanceof OAuthRegisteredService) {
            bean.setType(RegisteredServiceTypeEditBean.OAUTH.toString());
            final OAuthRegisteredService oauth = (OAuthRegisteredService) svc;
            final RegisteredServiceOAuthTypeEditBean oauthBean = bean.getOauth();
            oauthBean.setBypass(oauth.isBypassApprovalPrompt());
            oauthBean.setClientId(oauth.getClientId());
            oauthBean.setClientSecret(oauth.getClientSecret());
            oauthBean.setRefreshToken(oauth.isGenerateRefreshToken());
            oauthBean.setJsonFormat(oauth.isJsonFormat());

            if (svc instanceof OidcRegisteredService) {
                bean.setType(RegisteredServiceTypeEditBean.OIDC.toString());
                final OidcRegisteredService oidc = (OidcRegisteredService) svc;
                final RegisteredServiceOidcTypeEditBean oidcBean = bean.getOidc();
                oidcBean.setJwks(oidc.getJwks());
                oidcBean.setSignToken(oidc.isSignIdToken());
                oidcBean.setImplicit(oidc.isImplicit());

                oidcBean.setEncrypt(oidc.isEncryptIdToken());
                oidcBean.setEncryptAlg(oidc.getIdTokenEncryptionAlg());
                oidcBean.setEncryptEnc(oidc.getIdTokenEncryptionEncoding());

                oidcBean.setDynamic(oidc.isDynamicallyRegistered());
                if (oidc.isDynamicallyRegistered()) {
                    oidcBean.setDynamicDate(oidc.getDynamicRegistrationDateTime().toString());
                }

                oidcBean.setScopes(oidc.getScopes().stream().collect(Collectors.joining(",")));
            }

        }

        if (svc instanceof SamlRegisteredService) {
            bean.setType(RegisteredServiceTypeEditBean.SAML.toString());
            final SamlRegisteredService saml = (SamlRegisteredService) svc;
            final RegisteredServiceSamlTypeEditBean samlbean = bean.getSaml();
            samlbean.setMdLoc(saml.getMetadataLocation());
            samlbean.setMdMaxVal(saml.getMetadataMaxValidity());
            samlbean.setMdSigLoc(saml.getMetadataSignatureLocation());
            samlbean.setAuthCtxCls(saml.getRequiredAuthenticationContextClass());
            samlbean.setEncAssert(saml.isEncryptAssertions());
            samlbean.setSignResp(saml.isSignResponses());
            samlbean.setSignAssert(saml.isSignAssertions());
            samlbean.setRemoveEmptyEntities(saml.isMetadataCriteriaRemoveEmptyEntitiesDescriptors());
            samlbean.setRemoveRoleless(saml.isMetadataCriteriaRemoveRolelessEntityDescriptors());

            if (StringUtils.isNotBlank(saml.getMetadataCriteriaDirection())) {
                samlbean.setDir(saml.getMetadataCriteriaDirection().toUpperCase());
            }
            if (StringUtils.isNotBlank(saml.getMetadataCriteriaPattern())) {
                samlbean.setMdPattern(saml.getMetadataCriteriaPattern());
            }
            if (StringUtils.isNotBlank(saml.getMetadataCriteriaRoles())) {
                samlbean.setRoles(org.springframework.util.StringUtils.commaDelimitedListToSet(saml.getMetadataCriteriaRoles()));
            }

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

        final Map<String, RegisteredServiceProperty> props = svc.getProperties();
        final Set<RegisteredServiceEditBean.ServiceData.PropertyBean> beanProps = bean.getProperties();
        props.entrySet().forEach(p -> {
            final String set = org.springframework.util.StringUtils.collectionToCommaDelimitedString(p.getValue().getValues());
            beanProps.add(new RegisteredServiceEditBean.ServiceData.PropertyBean(p.getKey(), set));
        });

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
    public RegisteredService toRegisteredService(final RegisteredServiceEditBean.ServiceData data) {
        try {
            final AbstractRegisteredService regSvc;

            // create base RegisteredService object
            final String type = data.getType();
            if (StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.OAUTH.toString())
                    || StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.OIDC.toString())) {

                if (StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.OAUTH.toString())) {
                    regSvc = new OAuthRegisteredService();
                } else {
                    regSvc = new OidcRegisteredService();
                }

                final RegisteredServiceOAuthTypeEditBean oauthBean = data.getOauth();
                ((OAuthRegisteredService) regSvc).setClientId(oauthBean.getClientId());
                ((OAuthRegisteredService) regSvc).setClientSecret(oauthBean.getClientSecret());
                ((OAuthRegisteredService) regSvc).setBypassApprovalPrompt(oauthBean.isBypass());
                ((OAuthRegisteredService) regSvc).setGenerateRefreshToken(oauthBean.isRefreshToken());
                ((OAuthRegisteredService) regSvc).setJsonFormat(oauthBean.isJsonFormat());

                if (StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.OIDC.toString())) {
                    ((OidcRegisteredService) regSvc).setJwks(data.getOidc().getJwks());
                    ((OidcRegisteredService) regSvc).setSignIdToken(data.getOidc().isSignToken());
                    ((OidcRegisteredService) regSvc).setImplicit(data.getOidc().isImplicit());
                    ((OidcRegisteredService) regSvc).setEncryptIdToken(data.getOidc().isEncrypt());
                    ((OidcRegisteredService) regSvc).setIdTokenEncryptionAlg(data.getOidc().getEncryptAlg());
                    ((OidcRegisteredService) regSvc).setIdTokenEncryptionEncoding(data.getOidc().getEncryptEnc());
                    ((OidcRegisteredService) regSvc).setScopes(
                            org.springframework.util.StringUtils.commaDelimitedListToSet(data.getOidc().getScopes()));
                }
            } else if (StringUtils.equalsIgnoreCase(type, RegisteredServiceTypeEditBean.SAML.toString())) {
                regSvc = new SamlRegisteredService();

                final RegisteredServiceSamlTypeEditBean samlBean = data.getSaml();
                ((SamlRegisteredService) regSvc).setEncryptAssertions(samlBean.isEncAssert());
                ((SamlRegisteredService) regSvc).setSignAssertions(samlBean.isSignAssert());
                ((SamlRegisteredService) regSvc).setSignResponses(samlBean.isSignResp());
                ((SamlRegisteredService) regSvc).setMetadataLocation(samlBean.getMdLoc());
                ((SamlRegisteredService) regSvc).setMetadataSignatureLocation(samlBean.getMdSigLoc());
                ((SamlRegisteredService) regSvc).setMetadataMaxValidity(samlBean.getMdMaxVal());
                ((SamlRegisteredService) regSvc).setRequiredAuthenticationContextClass(samlBean.getAuthCtxCls());

                ((SamlRegisteredService) regSvc).setMetadataCriteriaRemoveEmptyEntitiesDescriptors(samlBean.isRemoveEmptyEntities());
                ((SamlRegisteredService) regSvc).setMetadataCriteriaRemoveRolelessEntityDescriptors(samlBean.isRemoveRoleless());

                if (StringUtils.isNotBlank(samlBean.getDir())) {
                    ((SamlRegisteredService) regSvc).setMetadataCriteriaDirection(samlBean.getDir().toUpperCase());
                }
                if (StringUtils.isNotBlank(samlBean.getMdPattern()) && RegexUtils.isValidRegex(samlBean.getMdPattern())) {
                    ((SamlRegisteredService) regSvc).setMetadataCriteriaPattern(samlBean.getMdPattern());
                }

                if (samlBean.getRoles() != null && !samlBean.getRoles().isEmpty()) {
                    ((SamlRegisteredService) regSvc).setMetadataCriteriaRoles(
                            org.springframework.util.StringUtils.collectionToCommaDelimitedString(samlBean.getRoles())
                    );
                }
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
            regSvc.setPrivacyUrl(data.getPrivacyUrl());
            regSvc.setInformationUrl(data.getInformationUrl());

            // process logout settings
            regSvc.setLogoutType(parseLogoutType(data.getLogoutType()));
            if (StringUtils.isNotBlank(data.getLogoutUrl())) {
                regSvc.setLogoutUrl(new URL(data.getLogoutUrl()));
            }

            // process the Public Key
            final RegisteredServicePublicKeyEditBean publicKey = data.getPublicKey();
            if (publicKey != null && publicKey.isValid()) {
                regSvc.setPublicKey(new RegisteredServicePublicKeyImpl(publicKey.getLocation(), publicKey
                        .getAlgorithm()));
            }

            final Set<RegisteredServiceEditBean.ServiceData.PropertyBean> props = data.getProperties();
            props.forEach(str -> {
                final DefaultRegisteredServiceProperty value = new DefaultRegisteredServiceProperty();
                value.setValues(org.springframework.util.StringUtils.commaDelimitedListToSet(str.getValue()));
                regSvc.getProperties().put(str.getName(), value);
            });


            return regSvc;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Parse raw logout type string to {@link LogoutType}.
     *
     * @param logoutType the reg svc
     */
    private static LogoutType parseLogoutType(final String logoutType) {
        if (StringUtils.equalsIgnoreCase(logoutType, RegisteredServiceLogoutTypeEditBean.BACK.toString())) {
            return LogoutType.BACK_CHANNEL;
        }
        if (StringUtils.equalsIgnoreCase(logoutType, RegisteredServiceLogoutTypeEditBean.FRONT.toString())) {
            return LogoutType.FRONT_CHANNEL;
        }
        return LogoutType.NONE;
    }
}
