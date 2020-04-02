package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.EntityRoleFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.io.Resource;

import javax.xml.namespace.QName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link BaseSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseSamlRegisteredServiceMetadataResolver implements SamlRegisteredServiceMetadataResolver {
    /**
     * The Saml idp properties.
     */
    protected final SamlIdPProperties samlIdPProperties;
    /**
     * The config bean.
     */
    protected final OpenSamlConfigBean configBean;

    private static void buildEntityRoleFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaRoles())) {
            val roles = new ArrayList<QName>();
            val rolesSet = org.springframework.util.StringUtils.commaDelimitedListToSet(service.getMetadataCriteriaRoles());
            rolesSet.forEach(s -> {
                if (s.equalsIgnoreCase(SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    LOGGER.debug("Added entity role filter [{}]", SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
                if (s.equalsIgnoreCase(IDPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    LOGGER.debug("Added entity role filter [{}]", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
            });
            val filter = new EntityRoleFilter(roles);
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

            val dir = PredicateFilter.Direction.valueOf(service.getMetadataCriteriaDirection());
            LOGGER.debug("Metadata predicate filter configuring with direction [{}] and pattern [{}]",
                service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());

            val filter = new PredicateFilter(dir, entityDescriptor ->
                StringUtils.isNotBlank(entityDescriptor.getEntityID()) && entityDescriptor.getEntityID().matches(service.getMetadataCriteriaPattern()));

            metadataFilterList.add(filter);
            LOGGER.debug("Added metadata predicate filter with direction [{}] and pattern [{}]",
                service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());
        }
    }

    /**
     * Build metadata resolver from document.
     *
     * @param service          the service
     * @param metadataDocument the metadata document
     * @return the metadata resolver
     */
    protected AbstractMetadataResolver buildMetadataResolverFrom(final SamlRegisteredService service,
                                                                 final SamlMetadataDocument metadataDocument) {
        try {
            val desc = StringUtils.defaultString(service.getDescription(), service.getName());
            val metadataResource = ResourceUtils.buildInputStreamResourceFrom(metadataDocument.getDecodedValue(), desc);
            val metadataResolver = new InMemoryResourceMetadataResolver(metadataResource, configBean);

            val metadataFilterList = new ArrayList<MetadataFilter>(1);
            if (StringUtils.isNotBlank(metadataDocument.getSignature())) {
                val signatureResource = ResourceUtils.buildInputStreamResourceFrom(metadataDocument.getSignature(), desc);
                buildSignatureValidationFilterIfNeeded(service, metadataFilterList, signatureResource);
            }
            configureAndInitializeSingleMetadataResolver(metadataResolver, service, metadataFilterList);
            return metadataResolver;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Build single metadata resolver metadata resolver.
     *
     * @param metadataProvider   the metadata provider
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     * @throws Exception the exception
     */
    protected void configureAndInitializeSingleMetadataResolver(final AbstractMetadataResolver metadataProvider,
                                                                final SamlRegisteredService service,
                                                                final List<MetadataFilter> metadataFilterList) throws Exception {
        val md = samlIdPProperties.getMetadata();
        metadataProvider.setParserPool(this.configBean.getParserPool());
        metadataProvider.setFailFastInitialization(md.isFailFast());
        metadataProvider.setRequireValidMetadata(md.isRequireValidMetadata());
        metadataProvider.setId(metadataProvider.getClass().getCanonicalName());

        buildMetadataFilters(service, metadataProvider, metadataFilterList);

        LOGGER.debug("Initializing metadata resolver from [{}]", service.getMetadataLocation());
        metadataProvider.initialize();
        LOGGER.info("Initialized metadata resolver from [{}]", service.getMetadataLocation());
    }

    /**
     * Configure and initialize single metadata resolver.
     *
     * @param metadataProvider the metadata provider
     * @param service          the service
     * @throws Exception the exception
     */
    protected void configureAndInitializeSingleMetadataResolver(final AbstractMetadataResolver metadataProvider,
                                                                final SamlRegisteredService service) throws Exception {
        configureAndInitializeSingleMetadataResolver(metadataProvider, service, new ArrayList<>(0));
    }

    /**
     * Build metadata filters.
     *
     * @param service            the service
     * @param metadataProvider   the metadata provider
     * @param metadataFilterList the metadata filter list
     * @throws Exception the exception
     */
    protected void buildMetadataFilters(final SamlRegisteredService service, final AbstractMetadataResolver metadataProvider,
                                        final List<MetadataFilter> metadataFilterList) throws Exception {
        buildRequiredValidUntilFilterIfNeeded(service, metadataFilterList);
        buildSignatureValidationFilterIfNeeded(service, metadataFilterList);

        buildEntityRoleFilterIfNeeded(service, metadataFilterList);
        buildPredicateFilterIfNeeded(service, metadataFilterList);

        if (!metadataFilterList.isEmpty()) {
            addMetadataFiltersToMetadataResolver(metadataProvider, metadataFilterList);
        }
    }

    /**
     * Add metadata filters to metadata resolver.
     *
     * @param metadataProvider   the metadata provider
     * @param metadataFilterList the metadata filter list
     */
    protected void addMetadataFiltersToMetadataResolver(final AbstractMetadataResolver metadataProvider, final MetadataFilter... metadataFilterList) {
        addMetadataFiltersToMetadataResolver(metadataProvider, Arrays.stream(metadataFilterList).collect(Collectors.toList()));
    }

    /**
     * Add metadata filters to metadata resolver.
     *
     * @param metadataProvider   the metadata provider
     * @param metadataFilterList the metadata filter list
     */
    protected void addMetadataFiltersToMetadataResolver(final AbstractMetadataResolver metadataProvider, final List<MetadataFilter> metadataFilterList) {
        val metadataFilterChain = new MetadataFilterChain();
        metadataFilterChain.setFilters(metadataFilterList);

        LOGGER.debug("Metadata filter chain initialized with [{}] filters", metadataFilterList.size());
        metadataProvider.setMetadataFilter(metadataFilterChain);
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     * @throws Exception the exception
     */
    protected static void buildSignatureValidationFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) throws Exception {
        if (StringUtils.isBlank(service.getMetadataSignatureLocation())) {
            LOGGER.warn("No metadata signature location is defined for [{}], so SignatureValidationFilter will not be invoked", service.getMetadataLocation());
            return;
        }
        buildSignatureValidationFilterIfNeeded(service, metadataFilterList, service.getMetadataSignatureLocation());
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param service                   the service
     * @param metadataFilterList        the metadata filter list
     * @param metadataSignatureResource the metadata signature resource
     * @throws Exception the exception
     */
    protected static void buildSignatureValidationFilterIfNeeded(final SamlRegisteredService service,
                                                                 final List<MetadataFilter> metadataFilterList,
                                                                 final String metadataSignatureResource) throws Exception {
        val signatureValidationFilter = SamlUtils.buildSignatureValidationFilter(metadataSignatureResource);
        signatureValidationFilter.setRequireSignedRoot(service.isRequireSignedRoot());
        addSignatureValidationFilterIfNeeded(service, signatureValidationFilter, metadataFilterList);
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param service                   the service
     * @param metadataFilterList        the metadata filter list
     * @param metadataSignatureResource the metadata signature resource
     * @throws Exception the exception
     */
    protected static void buildSignatureValidationFilterIfNeeded(final SamlRegisteredService service,
                                                                 final List<MetadataFilter> metadataFilterList,
                                                                 final Resource metadataSignatureResource) throws Exception {
        val signatureValidationFilter = SamlUtils.buildSignatureValidationFilter(metadataSignatureResource);
        addSignatureValidationFilterIfNeeded(service, signatureValidationFilter, metadataFilterList);
    }

    private static void addSignatureValidationFilterIfNeeded(final SamlRegisteredService service,
                                                             final SignatureValidationFilter signatureValidationFilter,
                                                             final List<MetadataFilter> metadataFilterList) {
        if (signatureValidationFilter != null) {
            signatureValidationFilter.setRequireSignedRoot(false);
            metadataFilterList.add(signatureValidationFilter);
            LOGGER.debug("Added metadata SignatureValidationFilter [{}] for [{}]", signatureValidationFilter, service.getServiceId());
        } else {
            LOGGER.warn("Skipped metadata SignatureValidationFilter since signature cannot be located for [{}]", service.getServiceId());
        }
    }

    /**
     * Build required valid until filter if needed. See {@link RequiredValidUntilFilter}.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     */
    protected void buildRequiredValidUntilFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (service.getMetadataMaxValidity() > 0) {
            val filter = new RequiredValidUntilFilter();
            filter.setMaxValidityInterval(Duration.ofSeconds(service.getMetadataMaxValidity()));
            metadataFilterList.add(filter);
            LOGGER.debug("Added metadata RequiredValidUntilFilter with max validity of [{}]", service.getMetadataMaxValidity());
        } else {
            LOGGER.debug("No metadata maximum validity criteria is defined for [{}], so RequiredValidUntilFilter will not be invoked",
                service.getMetadataLocation());
        }
    }
}
