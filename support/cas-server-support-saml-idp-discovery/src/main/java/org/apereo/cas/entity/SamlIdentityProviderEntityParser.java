package org.apereo.cas.entity;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.hjson.JsonValue;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link SamlIdentityProviderEntityParser}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class SamlIdentityProviderEntityParser implements DisposableBean {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Getter
    private final Set<SamlIdentityProviderEntity> identityProviders = new LinkedHashSet<>();

    private FileWatcherService discoveryFeedResourceWatchers;

    public SamlIdentityProviderEntityParser(final Resource resource) throws Exception {
        if (importResource(resource) && ResourceUtils.isFile(resource)) {
            discoveryFeedResourceWatchers = new FileWatcherService(resource.getFile(), file -> {
                try {
                    LOGGER.trace("Reloading identity providers...");
                    clear();
                    importResource(resource);
                } catch (final Exception e) {
                    LoggingUtils.error(LOGGER, e);
                }
            });
            discoveryFeedResourceWatchers.start(getClass().getSimpleName());
        }
    }

    public SamlIdentityProviderEntityParser(final SamlIdentityProviderEntity... entity) {
        this(Arrays.asList(entity));
    }

    public SamlIdentityProviderEntityParser(final List<SamlIdentityProviderEntity> entity) {
        identityProviders.addAll(entity);
    }

    /**
     * Clear providers.
     */
    public void clear() {
        identityProviders.clear();
    }

    /**
     * Import resource and provide boolean.
     *
     * @param resource the resource
     * @return true/false
     */
    public boolean importResource(final Resource resource) {
        try {
            if (ResourceUtils.doesResourceExist(resource)) {
                try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    val ref = new TypeReference<List<SamlIdentityProviderEntity>>() {
                    };
                    identityProviders.addAll(MAPPER.readValue(JsonValue.readHjson(reader).toString(), ref));
                }
                return true;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public void destroy() {
        if (discoveryFeedResourceWatchers != null) {
            discoveryFeedResourceWatchers.close();
        }
    }

    /**
     * Import from saml identity provider.
     *
     * @param client the client
     * @return the saml identity provider entity parser
     * @throws Exception the exception
     */
    public static SamlIdentityProviderEntityParser fromClient(final SAML2Client client) throws Exception {
        LOGGER.trace("Initializing SAML2 identity provider [{}]", client.getName());
        client.init();
        val entities = determineEntityDescriptors(client)
            .stream()
            .filter(EntityDescriptor::isValid)
            .map(entityDescriptor -> {
                val entity = new SamlIdentityProviderEntity();
                entity.setEntityID(entityDescriptor.getEntityID());
                
                val idpSSODescriptor = entityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
                Optional.ofNullable(idpSSODescriptor)
                    .map(IDPSSODescriptor::getExtensions)
                    .map(ext -> ext.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME))
                    .stream()
                    .flatMap(List::stream)
                    .map(UIInfo.class::cast)
                    .forEach(uiInfo -> {
                        uiInfo.getDisplayNames().forEach(value ->
                            entity.getDisplayNames().add(SamlIdentityProviderBasicEntity.builder().lang(value.getXMLLang()).value(value.getValue()).build()));
                        uiInfo.getDescriptions().forEach(value ->
                            entity.getDescriptions().add(SamlIdentityProviderBasicEntity.builder().lang(value.getXMLLang()).value(value.getValue()).build()));
                        uiInfo.getKeywords().forEach(value ->
                            entity.getKeywords().add(SamlIdentityProviderBasicEntity.builder().lang(value.getXMLLang())
                                .value(org.springframework.util.StringUtils.collectionToCommaDelimitedString(value.getKeywords())).build()));
                        uiInfo.getInformationURLs().forEach(value ->
                            entity.getInformationUrls().add(SamlIdentityProviderBasicEntity.builder().lang(value.getXMLLang()).value(value.getURI()).build()));
                        uiInfo.getPrivacyStatementURLs().forEach(value ->
                            entity.getPrivacyStatementUrls().add(SamlIdentityProviderBasicEntity.builder().lang(value.getXMLLang()).value(value.getURI()).build()));
                        uiInfo.getLogos().forEach(value ->
                            entity.getLogos().add(SamlIdentityProviderLogoEntity.builder().lang(value.getXMLLang())
                                .value(value.getURI()).width(value.getWidth()).height(value.getHeight()).build()));
                    });

                if (entity.getDisplayNames().isEmpty()) {
                    val clientDisplayName = client.getCustomProperties().containsKey(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME)
                        ? client.getCustomProperties().get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME).toString()
                        : client.getName();
                    entity.getDisplayNames().add(SamlIdentityProviderBasicEntity.builder()
                        .lang(Locale.ENGLISH.toLanguageTag()).value(clientDisplayName).build());
                }
                LOGGER.trace("Found SAML2 identity provider [{}]", entity);
                return entity;
            })
            .toList();

        return new SamlIdentityProviderEntityParser(entities);
    }

    private static List<EntityDescriptor> determineEntityDescriptors(final SAML2Client saml2Client) throws Exception {
        val idpMetadataResolver = saml2Client.getIdentityProviderMetadataResolver();
        val metadataResolver = idpMetadataResolver.resolve();
        val providers = metadataResolver.resolve(new CriteriaSet(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME)));
        if (Iterables.size(providers) == 1
            && idpMetadataResolver.getEntityDescriptorElement() instanceof final EntityDescriptor entityDescriptor) {
            return List.of(entityDescriptor);
        }
        return Arrays.asList(Iterables.toArray(providers, EntityDescriptor.class));
    }
}
