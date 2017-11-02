package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RegexUtils;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.EntityRoleFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is {@link BaseSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseSamlRegisteredServiceMetadataResolver implements SamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSamlRegisteredServiceMetadataResolver.class);

    /**
     * The Saml idp properties.
     */
    protected final SamlIdPProperties samlIdPProperties;
    /**
     * The config bean.
     */
    protected final OpenSamlConfigBean configBean;

    public BaseSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties, final OpenSamlConfigBean configBean) {
        this.samlIdPProperties = samlIdPProperties;
        this.configBean = configBean;
    }

    /**
     * Build single metadata resolver metadata resolver.
     *
     * @param metadataProvider the metadata provider
     * @param service          the service
     * @throws Exception the exception
     */
    protected void buildSingleMetadataResolver(final AbstractMetadataResolver metadataProvider,
                                               final SamlRegisteredService service) throws Exception {
        final SamlIdPProperties.Metadata md = samlIdPProperties.getMetadata();
        metadataProvider.setParserPool(this.configBean.getParserPool());
        metadataProvider.setFailFastInitialization(md.isFailFast());
        metadataProvider.setRequireValidMetadata(md.isRequireValidMetadata());
        metadataProvider.setId(metadataProvider.getClass().getCanonicalName());

        buildMetadataFilters(service, metadataProvider);

        LOGGER.debug("Initializing metadata resolver from [{}]", service.getMetadataLocation());
        metadataProvider.initialize();
        LOGGER.info("Initialized metadata resolver from [{}]", service.getMetadataLocation());
    }

    /**
     * Build metadata filters.
     *
     * @param service          the service
     * @param metadataProvider the metadata provider
     * @throws Exception the exception
     */
    protected void buildMetadataFilters(final SamlRegisteredService service, final AbstractMetadataResolver metadataProvider) throws
            Exception {
        final List<MetadataFilter> metadataFilterList = new ArrayList<>();

        buildRequiredValidUntilFilterIfNeeded(service, metadataFilterList);
        buildSignatureValidationFilterIfNeeded(service, metadataFilterList);

        buildEntityRoleFilterIfNeeded(service, metadataFilterList);
        buildPredicateFilterIfNeeded(service, metadataFilterList);

        if (!metadataFilterList.isEmpty()) {
            final MetadataFilterChain metadataFilterChain = new MetadataFilterChain();
            metadataFilterChain.setFilters(metadataFilterList);

            LOGGER.debug("Metadata filter chain initialized with [{}] filters", metadataFilterList.size());
            metadataProvider.setMetadataFilter(metadataFilterChain);
        }
    }

    private static void buildEntityRoleFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaRoles())) {
            final List<QName> roles = new ArrayList<>();
            final Set<String> rolesSet = org.springframework.util.StringUtils.commaDelimitedListToSet(service.getMetadataCriteriaRoles());
            rolesSet.stream().forEach(s -> {
                if (s.equalsIgnoreCase(SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    LOGGER.debug("Added entity role filter [{}]", SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
                if (s.equalsIgnoreCase(IDPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    LOGGER.debug("Added entity role filter [{}]", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
            });
            final EntityRoleFilter filter = new EntityRoleFilter(roles);
            filter.setRemoveEmptyEntitiesDescriptors(service.isMetadataCriteriaRemoveEmptyEntitiesDescriptors());
            filter.setRemoveRolelessEntityDescriptors(service.isMetadataCriteriaRemoveRolelessEntityDescriptors());

            metadataFilterList.add(filter);
            LOGGER.debug("Added entity role filter with roles [{}]", roles);
        }
    }


    private static void buildPredicateFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaDirection())
                && StringUtils.isNotBlank(service.getMetadataCriteriaPattern())
                && RegexUtils.isValidRegex(service.getMetadataCriteriaPattern())) {

            final PredicateFilter.Direction dir = PredicateFilter.Direction.valueOf(service.getMetadataCriteriaDirection());
            LOGGER.debug("Metadata predicate filter configuring with direction [{}] and pattern [{}]",
                    service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());

            final PredicateFilter filter = new PredicateFilter(dir, entityDescriptor ->
                    StringUtils.isNotBlank(entityDescriptor.getEntityID()) && entityDescriptor.getEntityID().matches(service.getMetadataCriteriaPattern()));

            metadataFilterList.add(filter);
            LOGGER.debug("Added metadata predicate filter with direction [{}] and pattern [{}]",
                    service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());
        }
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     * @throws Exception the exception
     */
    protected void buildSignatureValidationFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter>
            metadataFilterList) throws Exception {
        if (StringUtils.isBlank(service.getMetadataSignatureLocation())) {
            LOGGER.warn("No metadata signature location is defined for [{}], so SignatureValidationFilter will not be invoked",
                    service.getMetadataLocation());
            return;
        }

        final SignatureValidationFilter signatureValidationFilter =
                SamlUtils.buildSignatureValidationFilter(service.getMetadataSignatureLocation());
        if (signatureValidationFilter != null) {
            signatureValidationFilter.setRequireSignedRoot(false);
            metadataFilterList.add(signatureValidationFilter);
            LOGGER.debug("Added metadata SignatureValidationFilter with signature from [{}]", service.getMetadataSignatureLocation());
        } else {
            LOGGER.warn("Skipped metadata SignatureValidationFilter since signature from [{}] cannot be located",
                    service.getMetadataLocation());
        }
    }

    /**
     * Build required valid until filter if needed. See {@link RequiredValidUntilFilter}.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     */
    protected void buildRequiredValidUntilFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter>
            metadataFilterList) {
        if (service.getMetadataMaxValidity() > 0) {
            final RequiredValidUntilFilter requiredValidUntilFilter = new RequiredValidUntilFilter(service.getMetadataMaxValidity());
            metadataFilterList.add(requiredValidUntilFilter);
            LOGGER.debug("Added metadata RequiredValidUntilFilter with max validity of [{}]", service.getMetadataMaxValidity());
        } else {
            LOGGER.debug("No metadata maximum validity criteria is defined for [{}], so RequiredValidUntilFilter will not be invoked",
                    service.getMetadataLocation());
        }
    }
}
