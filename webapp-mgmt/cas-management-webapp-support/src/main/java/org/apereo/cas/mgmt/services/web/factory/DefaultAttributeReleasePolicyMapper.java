package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.mgmt.services.web.beans.AbstractRegisteredServiceAttributeReleasePolicyStrategyBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyViewBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyViewBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcAddressScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcPhoneScopeAttributeReleasePolicy;
import org.apereo.cas.oidc.claims.OidcProfileScopeAttributeReleasePolicy;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Default mapper for converting {@link RegisteredServiceAttributeReleasePolicy} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultAttributeReleasePolicyMapper implements AttributeReleasePolicyMapper {

    private final AttributeFilterMapper attributeFilterMapper;
    private final PrincipalAttributesRepositoryMapper principalAttributesRepositoryMapper;
    private final Collection<BaseOidcScopeAttributeReleasePolicy> userDefinedScopeBasedAttributeReleasePolicies;

    public DefaultAttributeReleasePolicyMapper(final AttributeFilterMapper attributeFilterMapper,
                                               final PrincipalAttributesRepositoryMapper mapper,
                                               final Collection<BaseOidcScopeAttributeReleasePolicy> userDefinedScopeBasedAttributeReleasePolicies) {
        this.attributeFilterMapper = attributeFilterMapper;
        this.principalAttributesRepositoryMapper = mapper;
        this.userDefinedScopeBasedAttributeReleasePolicies = userDefinedScopeBasedAttributeReleasePolicies;
    }

    /**
     * Initializes some default mappers after any custom mappers have been wired.
     */
    @PostConstruct
    public void initializeDefaults() {
    }

    @Override
    public void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy, final RegisteredServiceEditBean.ServiceData bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy = (AbstractRegisteredServiceAttributeReleasePolicy) policy;
            final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();

            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());
            attrPolicyBean.setExcludeDefault(attrPolicy.isExcludeDefaultAttributes());

            this.attributeFilterMapper.mapAttributeFilter(attrPolicy.getAttributeFilter(), bean);
            this.principalAttributesRepositoryMapper.mapPrincipalRepository(attrPolicy.getPrincipalAttributesRepository(), bean);

            final RegisteredServiceAttributeReleasePolicyStrategyEditBean sBean = attrPolicyBean.getAttrPolicy();

            if (attrPolicy instanceof ScriptedRegisteredServiceAttributeReleasePolicy) {
                final ScriptedRegisteredServiceAttributeReleasePolicy policyS = (ScriptedRegisteredServiceAttributeReleasePolicy) attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.SCRIPT.toString());
                sBean.setScriptFile(policyS.getScriptFile());
            } else if (attrPolicy instanceof GroovyScriptAttributeReleasePolicy) {
                final GroovyScriptAttributeReleasePolicy policyG = (GroovyScriptAttributeReleasePolicy) attrPolicy;
                sBean.setScriptFile(policyG.getGroovyScript());
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.GROOVY.toString());
            } else if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALL.toString());
            } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
                final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALLOWED.toString());
                sBean.setAttributes(attrPolicyAllowed.getAllowedAttributes());
            } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
                final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.MAPPED.toString());
                sBean.setAttributes(attrPolicyAllowed.getAllowedAttributes());
            } else if (attrPolicy instanceof DenyAllAttributeReleasePolicy) {
                sBean.setType(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.DENY.toString());
            }
        }
    }

    @Override
    public void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy, final RegisteredServiceViewBean bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy = (AbstractRegisteredServiceAttributeReleasePolicy) policy;

            final RegisteredServiceAttributeReleasePolicyViewBean attrPolicyBean = bean.getAttrRelease();
            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());
            attrPolicyBean.setExcludeDefault(attrPolicy.isExcludeDefaultAttributes());

            if (attrPolicy instanceof ScriptedRegisteredServiceAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.SCRIPT.toString());
            } else if (attrPolicy instanceof GroovyScriptAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.GROOVY.toString());
            } else if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
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

        if ("oidc".equals(data.getType())) {
            return chainScopes(data.getOidc().getScopes());
        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.SCRIPT.toString())) {
            policy = new ScriptedRegisteredServiceAttributeReleasePolicy(policyBean.getScriptFile());

        } else if (StringUtils.equalsIgnoreCase(policyType,
                AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.GROOVY.toString())) {
            policy = new GroovyScriptAttributeReleasePolicy(policyBean.getScriptFile());

        } else if (StringUtils.equalsIgnoreCase(policyType, AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.ALL.toString())) {
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
        policy.setExcludeDefaultAttributes(attrRelease.isExcludeDefault());

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

    private RegisteredServiceAttributeReleasePolicy chainScopes(final String scopes) {
        final List<String> scopeList = Arrays.asList(scopes.split(","));
        final ChainingAttributeReleasePolicy policy = new ChainingAttributeReleasePolicy();
        
        scopeList.forEach(s -> {
            switch (s.trim().toLowerCase()) {
                case OidcConstants.EMAIL:
                    policy.getPolicies().add(new OidcEmailScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.ADDRESS:
                    policy.getPolicies().add(new OidcAddressScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.PROFILE:
                    policy.getPolicies().add(new OidcProfileScopeAttributeReleasePolicy());
                    break;
                case OidcConstants.PHONE:
                    policy.getPolicies().add(new OidcPhoneScopeAttributeReleasePolicy());
                    break;
                default:
                    final BaseOidcScopeAttributeReleasePolicy userPolicy = userDefinedScopeBasedAttributeReleasePolicies.stream()
                            .filter(t -> t.getScopeName().equals(s.trim()))
                            .findFirst()
                            .orElse(null);
                    if (userPolicy != null) {
                        policy.getPolicies().add(userPolicy);
                    }
            }
        });

        if (policy.getPolicies().isEmpty()) {
            return new DenyAllAttributeReleasePolicy();
        }

        return policy;
    }
}
