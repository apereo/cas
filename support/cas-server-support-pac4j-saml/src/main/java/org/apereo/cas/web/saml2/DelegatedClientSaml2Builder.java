package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.pac4j.authentication.attributes.GroovyAttributeConverter;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.metadata.DefaultSAML2MetadataSigner;
import org.pac4j.saml.metadata.SAML2ServiceProviderRequestedAttribute;
import org.pac4j.saml.store.EmptyStoreFactory;
import org.pac4j.saml.store.HttpSessionStoreFactory;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import java.time.Period;
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
    public BaseClient configure(final BaseClient client, final Pac4jBaseClientProperties clientProperties,
                                final CasConfigurationProperties properties) {
        if (client instanceof final SAML2Client saml2Client && saml2Client.isInitialized()) {
            saml2Client.getIdentityProviderMetadataResolver().resolve(true);
        }
        return client;
    }
}
