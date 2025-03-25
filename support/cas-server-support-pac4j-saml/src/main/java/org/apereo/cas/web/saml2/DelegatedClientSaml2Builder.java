package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.authentication.attributes.GroovyAttributeConverter;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractBatchMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.metadata.DefaultSAML2MetadataSigner;
import org.pac4j.saml.metadata.SAML2MetadataResolver;
import org.pac4j.saml.metadata.SAML2ServiceProviderRequestedAttribute;
import org.pac4j.saml.store.EmptyStoreFactory;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.pac4j.saml.util.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DelegatedClientSaml2Builder}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientSaml2Builder implements ConfigurableDelegatedClientBuilder {
    private final CasSSLContext casSslContext;
    private final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory;

    @Override
    public List<ConfigurableDelegatedClient> build(final CasConfigurationProperties casProperties) {
        return buildSaml2IdentityProviders(casProperties);
    }

    protected List<ConfigurableDelegatedClient> buildSaml2IdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        return pac4jProperties
            .getSaml()
            .stream()
            .filter(saml -> saml.isEnabled()
                && StringUtils.isNotBlank(saml.getMetadata().getIdentityProviderMetadataPath())
                && StringUtils.isNotBlank(saml.getServiceProviderEntityId()))
            .map(saml -> {
                val keystorePath = SpringExpressionLanguageValueResolver.getInstance().resolve(
                    StringUtils.defaultIfBlank(saml.getKeystorePath(), Beans.getTempFilePath("samlSpKeystore", ".jks")));
                val identityProviderMetadataPath = SpringExpressionLanguageValueResolver.getInstance()
                    .resolve(saml.getMetadata().getIdentityProviderMetadataPath());
                LOGGER.debug("Creating SAML2 identity provider [{}] with identity provider metadata [{}]",
                    saml.getClientName(), identityProviderMetadataPath);

                val configuration = new SAML2Configuration(keystorePath, saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), identityProviderMetadataPath);
                configuration.setForceKeystoreGeneration(saml.isForceKeystoreGeneration());

                FunctionUtils.doIf(saml.getCertificateExpirationDays() > 0,
                    __ -> configuration.setCertificateExpirationPeriod(Period.ofDays(saml.getCertificateExpirationDays()))).accept(saml);
                FunctionUtils.doIfNotNull(saml.getResponseBindingType(), configuration::setResponseBindingType);
                FunctionUtils.doIfNotNull(saml.getCertificateSignatureAlg(), configuration::setCertificateSignatureAlg);

                configuration.setPartialLogoutTreatedAsSuccess(saml.isPartialLogoutAsSuccess());
                configuration.setResponseDestinationAttributeMandatory(saml.isResponseDestinationMandatory());
                configuration.setSupportedProtocols(saml.getSupportedProtocols());

                FunctionUtils.doIfNotBlank(saml.getRequestInitiatorUrl(), __ -> configuration.setRequestInitiatorUrl(saml.getRequestInitiatorUrl()));
                FunctionUtils.doIfNotBlank(saml.getSingleLogoutServiceUrl(), __ -> configuration.setSingleSignOutServiceUrl(saml.getSingleLogoutServiceUrl()));
                FunctionUtils.doIfNotBlank(saml.getLogoutResponseBindingType(), __ -> configuration.setSpLogoutResponseBindingType(saml.getLogoutResponseBindingType()));

                configuration.setCertificateNameToAppend(StringUtils.defaultIfBlank(saml.getCertificateNameToAppend(), saml.getClientName()));
                configuration.setMaximumAuthenticationLifetime(Beans.newDuration(saml.getMaximumAuthenticationLifetime()).toSeconds());
                val serviceProviderEntityId = SpringExpressionLanguageValueResolver.getInstance().resolve(saml.getServiceProviderEntityId());
                configuration.setServiceProviderEntityId(serviceProviderEntityId);

                val samlSpMetadata = StringUtils.defaultIfBlank(saml.getMetadata().getServiceProvider().getFileSystem().getLocation(),
                    Beans.getTempFilePath("samlSpMetadata", ".xml"));
                FunctionUtils.doIfNotNull(samlSpMetadata, location -> {
                    val resource = ResourceUtils.getRawResourceFrom(location);
                    LOGGER.debug("Service provider metadata is located at [{}] with entity id [{}]", resource, serviceProviderEntityId);
                    configuration.setServiceProviderMetadataResource(resource);
                });

                configuration.setAuthnRequestBindingType(saml.getDestinationBinding());
                configuration.setSpLogoutRequestBindingType(saml.getLogoutRequestBinding());
                configuration.setForceAuth(saml.isForceAuth());
                configuration.setPassive(saml.isPassive());
                configuration.setSignMetadata(saml.isSignServiceProviderMetadata());
                configuration.setMetadataSigner(new DefaultSAML2MetadataSigner(configuration));
                configuration.setAuthnRequestSigned(saml.isSignAuthnRequest());
                configuration.setSpLogoutRequestSigned(saml.isSignServiceProviderLogoutRequest());
                configuration.setAcceptedSkew(Beans.newDuration(saml.getAcceptedSkew()).toSeconds());
                configuration.setSslSocketFactory(casSslContext.getSslContext().getSocketFactory());
                configuration.setHostnameVerifier(casSslContext.getHostnameVerifier());

                FunctionUtils.doIfNotBlank(saml.getPrincipalIdAttribute(), __ -> configuration.setAttributeAsId(saml.getPrincipalIdAttribute()));
                FunctionUtils.doIfNotBlank(saml.getNameIdAttribute(), __ -> configuration.setNameIdAttribute(saml.getNameIdAttribute()));

                configuration.setWantsAssertionsSigned(saml.isWantsAssertionsSigned());
                configuration.setWantsResponsesSigned(saml.isWantsResponsesSigned());
                configuration.setAllSignatureValidationDisabled(saml.isAllSignatureValidationDisabled());
                configuration.setUseNameQualifier(saml.isUseNameQualifier());
                configuration.setAttributeConsumingServiceIndex(saml.getAttributeConsumingServiceIndex());

                Optional.ofNullable(samlMessageStoreFactory.getIfAvailable())
                    .ifPresentOrElse(configuration::setSamlMessageStoreFactory, () -> {
                        FunctionUtils.doIf("EMPTY".equalsIgnoreCase(saml.getMessageStoreFactory()),
                            ig -> configuration.setSamlMessageStoreFactory(new EmptyStoreFactory())).accept(saml);
                        FunctionUtils.doIf("SESSION".equalsIgnoreCase(saml.getMessageStoreFactory()),
                            ig -> configuration.setSamlMessageStoreFactory(new HttpSessionStoreFactory())).accept(saml);
                        if (saml.getMessageStoreFactory().contains(".")) {
                            FunctionUtils.doAndHandle(__ -> {
                                val clazz = ClassUtils.getClass(getClass().getClassLoader(), saml.getMessageStoreFactory());
                                val factory = (SAMLMessageStoreFactory) clazz.getDeclaredConstructor().newInstance();
                                configuration.setSamlMessageStoreFactory(factory);
                            });
                        }
                    });

                FunctionUtils.doIf(saml.getAssertionConsumerServiceIndex() >= 0,
                    __ -> configuration.setAssertionConsumerServiceIndex(saml.getAssertionConsumerServiceIndex())).accept(saml);

                if (!saml.getAuthnContextClassRef().isEmpty()) {
                    configuration.setComparisonType(saml.getAuthnContextComparisonType().toUpperCase(Locale.ENGLISH));
                    configuration.setAuthnContextClassRefs(saml.getAuthnContextClassRef());
                }

                FunctionUtils.doIfNotBlank(saml.getNameIdPolicyFormat(), __ -> configuration.setNameIdPolicyFormat(saml.getNameIdPolicyFormat()));

                if (!saml.getRequestedAttributes().isEmpty()) {
                    saml.getRequestedAttributes().stream()
                        .map(attribute -> new SAML2ServiceProviderRequestedAttribute(attribute.getName(), attribute.getFriendlyName(),
                            attribute.getNameFormat(), attribute.isRequired()))
                        .forEach(attribute -> configuration.getRequestedServiceProviderAttributes().add(attribute));
                }

                if (!saml.getBlockedSignatureSigningAlgorithms().isEmpty()) {
                    configuration.setBlackListedSignatureSigningAlgorithms(saml.getBlockedSignatureSigningAlgorithms());
                }
                if (!saml.getSignatureAlgorithms().isEmpty()) {
                    configuration.setSignatureAlgorithms(saml.getSignatureAlgorithms());
                }
                if (!saml.getSignatureReferenceDigestMethods().isEmpty()) {
                    configuration.setSignatureReferenceDigestMethods(saml.getSignatureReferenceDigestMethods());
                }

                FunctionUtils.doIfNotBlank(saml.getSignatureCanonicalizationAlgorithm(),
                    __ -> configuration.setSignatureCanonicalizationAlgorithm(saml.getSignatureCanonicalizationAlgorithm()));
                configuration.setProviderName(saml.getProviderName());
                configuration.setNameIdPolicyAllowCreate(saml.getNameIdPolicyAllowCreate().toBoolean());

                if (StringUtils.isNotBlank(saml.getSaml2AttributeConverter())) {
                    if (scriptFactory.isPresent() && scriptFactory.get().isExternalScript(saml.getSaml2AttributeConverter())) {
                        FunctionUtils.doAndHandle(__ -> {
                            val resource = ResourceUtils.getResourceFrom(saml.getSaml2AttributeConverter());
                            val script = scriptFactory.get().fromResource(resource);
                            configuration.setSamlAttributeConverter(new GroovyAttributeConverter(script));
                        });
                    } else {
                        FunctionUtils.doAndHandle(__ -> {
                            val clazz = ClassUtils.getClass(getClass().getClassLoader(), saml.getSaml2AttributeConverter());
                            val converter = (AttributeConverter) clazz.getDeclaredConstructor().newInstance();
                            configuration.setSamlAttributeConverter(converter);
                        });
                    }
                }

                val mappedAttributes = saml.getMappedAttributes();
                if (!mappedAttributes.isEmpty()) {
                    configuration.setMappedAttributes(CollectionUtils.convertDirectedListToMap(mappedAttributes));
                }

                val client = new SAML2Client(configuration);
                LOGGER.debug("Created SAML2 delegated client [{}]", client);
                return new ConfigurableDelegatedClient(client, saml);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<? extends BaseClient> configure(final BaseClient client,
                                                final Pac4jBaseClientProperties clientProperties,
                                                final CasConfigurationProperties properties) throws Exception {
        if (client instanceof final SAML2Client saml2Client && clientProperties instanceof Pac4jSamlClientProperties saml2Properties) {
            LOGGER.info("Loading SAML2 identity provider metadata from [{}]",
                saml2Properties.getMetadata().getIdentityProviderMetadataPath());
            saml2Client.init(saml2Client.getIdentityProviderMetadataResolver() == null || !saml2Client.isInitialized());
            LOGGER.info("Loaded SAML2 identity provider metadata from [{}]",
                saml2Properties.getMetadata().getIdentityProviderMetadataPath());
            val idpMetadataResolver = saml2Client.getIdentityProviderMetadataResolver();

            val metadataResolver = idpMetadataResolver.resolve();
            val providers = metadataResolver.resolve(new CriteriaSet(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME)));
            if (Iterables.size(providers) > 1) {
                return Arrays.stream(Iterables.toArray(providers, EntityDescriptor.class))
                    .parallel()
                    .filter(EntityDescriptor::isValid)
                    .map(entityDescriptor -> {
                        val configuration = saml2Client.getConfiguration();
                        LOGGER.trace("Loading SAML2 client for identity provider with entity id [{}]", entityDescriptor.getEntityID());
                        val singleClient = new SAML2Client(
                            configuration
                                .withIdentityProviderEntityId(entityDescriptor.getEntityID())
                                .withIdentityProviderMetadataResolver(new DelegatingSaml2MetadataResolver(metadataResolver, entityDescriptor))
                        );  

                        DelegatedIdentityProviderFactory.configureClientName(singleClient, client.getName() + '-' + RandomUtils.nextLong());
                        DelegatedIdentityProviderFactory.configureClientCustomProperties(singleClient, saml2Properties);
                        DelegatedIdentityProviderFactory.configureClientCallbackUrl(singleClient, saml2Properties, properties.getServer().getLoginUrl());
                        singleClient.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_IDENTITY_PROVIDER_METADATA_AGGREGATE, true);
                        
                        val idpSSODescriptor = entityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
                        Optional.ofNullable(idpSSODescriptor)
                            .map(IDPSSODescriptor::getExtensions)
                            .map(ext -> ext.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME))
                            .stream()
                            .flatMap(List::stream)
                            .map(UIInfo.class::cast)
                            .filter(uiInfo -> !uiInfo.getDisplayNames().isEmpty())
                            .map(uiInfo -> uiInfo.getDisplayNames().getFirst().getValue())
                            .filter(StringUtils::isNotBlank)
                            .forEach(value -> singleClient.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, value));

                        singleClient.init();
                        return singleClient;
                    })
                    .toList();
            }
        }
        return ConfigurableDelegatedClientBuilder.super.configure(client, clientProperties, properties);
    }

    @RequiredArgsConstructor
    @Getter
    private static final class DelegatingSaml2MetadataResolver implements SAML2MetadataResolver {
        private final MetadataResolver delegate;
        private final XMLObject entityDescriptorElement;

        @Override
        public MetadataResolver resolve(final boolean force) {
            return delegate;
        }

        @Override
        public String getEntityId() {
            return ((EntityDescriptor) entityDescriptorElement).getEntityID();
        }

        @Override
        public String getMetadata() {
            return Configuration.serializeSamlObject(entityDescriptorElement).toString();
        }
    }
}
