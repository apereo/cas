package org.apereo.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;
import org.opensaml.saml.common.profile.logic.EntityAttributesPredicate;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link InCommonRSAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InCommonRSAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
    private static final long serialVersionUID = 1532960981124784595L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InCommonRSAttributeReleasePolicy.class);

    private List<String> allowedAttributes = Arrays.asList("eduPersonPrincipalName",
            "eduPersonTargetedID", "email", "displayName", "givenName", "surname",
            "eduPersonScopedAffiliation");

    public InCommonRSAttributeReleasePolicy() {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> attributes,
                                                        final RegisteredService service) {
        if (service instanceof SamlRegisteredService) {
            final SamlRegisteredService saml = (SamlRegisteredService) service;

            final EntityAttributesPredicate.Candidate attr =
                    new EntityAttributesPredicate.Candidate("http://macedir.org/entity-category");
            attr.setValues(Collections.singletonList("http://refeds.org/category/research-and-scholarship"));

            final EntityAttributesPredicate predicate = new EntityAttributesPredicate(
                    Collections.singletonList(attr), true);

            final HttpServletRequest request = WebUtils.getHttpServletRequestFromRequestAttributes();
            if (request == null) {
                LOGGER.warn("Could not locate the http request object to process attributes");
                return new HashMap<>();
            }

            String entityId = request.getParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                final String svcParam = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
                if (StringUtils.isNotBlank(svcParam)) {
                    try {
                        final URIBuilder builder = new URIBuilder(svcParam);
                        entityId = builder.getQueryParams().stream()
                                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                                .map(NameValuePair::getValue)
                                .findFirst()
                                .orElse(StringUtils.EMPTY);
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }

            if (StringUtils.isBlank(entityId)) {
                LOGGER.warn("Could not locate the entity id for this service to process attributes");
                return new HashMap<>();
            }
            
            final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            if (ctx == null) {
                LOGGER.warn("Could not locate the application context to process attributes");
                return new HashMap<>();
            }
            final SamlRegisteredServiceCachingMetadataResolver resolver =
                    ctx.getBean("defaultSamlRegisteredServiceCachingMetadataResolver",
                            SamlRegisteredServiceCachingMetadataResolver.class);

            final SamlRegisteredServiceServiceProviderMetadataFacade facade =
                    SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, saml, entityId);
            final EntityDescriptor input = facade.getEntityDescriptor();
            if (predicate.apply(input)) {
                return super.getAttributesInternal(attributes, service);
            }
        }
        return new HashMap();
    }

    @JsonIgnore
    @Override
    public List<String> getAllowedAttributes() {
        return super.getAllowedAttributes();
    }
}
