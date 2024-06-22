package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.pac4j.authentication.attributes.GroovyAttributeConverter;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final CasConfigurationProperties casProperties;

    @Override
    public List<ConfigurableDelegatedClient> build() {
        return buildSaml2IdentityProviders();
    }

    protected List<ConfigurableDelegatedClient> buildSaml2IdentityProviders() {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
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
                LOGGER.debug("Creating SAML2 identity provider [{}] with identity provider metadata [{}]", saml.getClientName(), identityProviderMetadataPath);
                
                val cfg = new SAML2Configuration(keystorePath, saml.getKeystorePassword(),
                    saml.getPrivateKeyPassword(), identityProviderMetadataPath);
                cfg.setForceKeystoreGeneration(saml.isForceKeystoreGeneration());

                FunctionUtils.doIf(saml.getCertificateExpirationDays() > 0,
                    __ -> cfg.setCertificateExpirationPeriod(Period.ofDays(saml.getCertificateExpirationDays()))).accept(saml);
                FunctionUtils.doIfNotNull(saml.getResponseBindingType(), cfg::setResponseBindingType);
                FunctionUtils.doIfNotNull(saml.getCertificateSignatureAlg(), cfg::setCertificateSignatureAlg);

                cfg.setPartialLogoutTreatedAsSuccess(saml.isPartialLogoutAsSuccess());
                cfg.setResponseDestinationAttributeMandatory(saml.isResponseDestinationMandatory());
                cfg.setSupportedProtocols(saml.getSupportedProtocols());

                FunctionUtils.doIfNotBlank(saml.getRequestInitiatorUrl(), __ -> cfg.setRequestInitiatorUrl(saml.getRequestInitiatorUrl()));
                FunctionUtils.doIfNotBlank(saml.getSingleLogoutServiceUrl(), __ -> cfg.setSingleSignOutServiceUrl(saml.getSingleLogoutServiceUrl()));
                FunctionUtils.doIfNotBlank(saml.getLogoutResponseBindingType(), __ -> cfg.setSpLogoutResponseBindingType(saml.getLogoutResponseBindingType()));

                cfg.setCertificateNameToAppend(StringUtils.defaultIfBlank(saml.getCertificateNameToAppend(), saml.getClientName()));
                cfg.setMaximumAuthenticationLifetime(Beans.newDuration(saml.getMaximumAuthenticationLifetime()).toSeconds());
                val serviceProviderEntityId = SpringExpressionLanguageValueResolver.getInstance().resolve(saml.getServiceProviderEntityId());
                cfg.setServiceProviderEntityId(serviceProviderEntityId);

                val samlSpMetadata = StringUtils.defaultIfBlank(saml.getMetadata().getServiceProvider().getFileSystem().getLocation(),
                    Beans.getTempFilePath("samlSpMetadata", ".xml"));
                FunctionUtils.doIfNotNull(samlSpMetadata, location -> {
                    val resource = ResourceUtils.getRawResourceFrom(location);
                    LOGGER.debug("Service provider metadata is located at [{}] with entity id [{}]", resource, serviceProviderEntityId);
                    cfg.setServiceProviderMetadataResource(resource);
                });

                cfg.setAuthnRequestBindingType(saml.getDestinationBinding());
                cfg.setSpLogoutRequestBindingType(saml.getLogoutRequestBinding());
                cfg.setForceAuth(saml.isForceAuth());
                cfg.setPassive(saml.isPassive());
                cfg.setSignMetadata(saml.isSignServiceProviderMetadata());
                cfg.setMetadataSigner(new DefaultSAML2MetadataSigner(cfg));
                cfg.setAuthnRequestSigned(saml.isSignAuthnRequest());
                cfg.setSpLogoutRequestSigned(saml.isSignServiceProviderLogoutRequest());
                cfg.setAcceptedSkew(Beans.newDuration(saml.getAcceptedSkew()).toSeconds());
                cfg.setSslSocketFactory(casSslContext.getSslContext().getSocketFactory());
                cfg.setHostnameVerifier(casSslContext.getHostnameVerifier());

                FunctionUtils.doIfNotBlank(saml.getPrincipalIdAttribute(), __ -> cfg.setAttributeAsId(saml.getPrincipalIdAttribute()));
                FunctionUtils.doIfNotBlank(saml.getNameIdAttribute(), __ -> cfg.setNameIdAttribute(saml.getNameIdAttribute()));

                cfg.setWantsAssertionsSigned(saml.isWantsAssertionsSigned());
                cfg.setWantsResponsesSigned(saml.isWantsResponsesSigned());
                cfg.setAllSignatureValidationDisabled(saml.isAllSignatureValidationDisabled());
                cfg.setUseNameQualifier(saml.isUseNameQualifier());
                cfg.setAttributeConsumingServiceIndex(saml.getAttributeConsumingServiceIndex());

                Optional.ofNullable(samlMessageStoreFactory.getIfAvailable())
                    .ifPresentOrElse(cfg::setSamlMessageStoreFactory, () -> {
                        FunctionUtils.doIf("EMPTY".equalsIgnoreCase(saml.getMessageStoreFactory()),
                            ig -> cfg.setSamlMessageStoreFactory(new EmptyStoreFactory())).accept(saml);
                        FunctionUtils.doIf("SESSION".equalsIgnoreCase(saml.getMessageStoreFactory()),
                            ig -> cfg.setSamlMessageStoreFactory(new HttpSessionStoreFactory())).accept(saml);
                        if (saml.getMessageStoreFactory().contains(".")) {
                            FunctionUtils.doAndHandle(__ -> {
                                val clazz = ClassUtils.getClass(getClass().getClassLoader(), saml.getMessageStoreFactory());
                                val factory = (SAMLMessageStoreFactory) clazz.getDeclaredConstructor().newInstance();
                                cfg.setSamlMessageStoreFactory(factory);
                            });
                        }
                    });

                FunctionUtils.doIf(saml.getAssertionConsumerServiceIndex() >= 0,
                    __ -> cfg.setAssertionConsumerServiceIndex(saml.getAssertionConsumerServiceIndex())).accept(saml);

                if (!saml.getAuthnContextClassRef().isEmpty()) {
                    cfg.setComparisonType(saml.getAuthnContextComparisonType().toUpperCase(Locale.ENGLISH));
                    cfg.setAuthnContextClassRefs(saml.getAuthnContextClassRef());
                }

                FunctionUtils.doIfNotBlank(saml.getNameIdPolicyFormat(), __ -> cfg.setNameIdPolicyFormat(saml.getNameIdPolicyFormat()));

                if (!saml.getRequestedAttributes().isEmpty()) {
                    saml.getRequestedAttributes().stream()
                        .map(attribute -> new SAML2ServiceProviderRequestedAttribute(attribute.getName(), attribute.getFriendlyName(),
                            attribute.getNameFormat(), attribute.isRequired()))
                        .forEach(attribute -> cfg.getRequestedServiceProviderAttributes().add(attribute));
                }

                if (!saml.getBlockedSignatureSigningAlgorithms().isEmpty()) {
                    cfg.setBlackListedSignatureSigningAlgorithms(saml.getBlockedSignatureSigningAlgorithms());
                }
                if (!saml.getSignatureAlgorithms().isEmpty()) {
                    cfg.setSignatureAlgorithms(saml.getSignatureAlgorithms());
                }
                if (!saml.getSignatureReferenceDigestMethods().isEmpty()) {
                    cfg.setSignatureReferenceDigestMethods(saml.getSignatureReferenceDigestMethods());
                }

                FunctionUtils.doIfNotBlank(saml.getSignatureCanonicalizationAlgorithm(),
                    __ -> cfg.setSignatureCanonicalizationAlgorithm(saml.getSignatureCanonicalizationAlgorithm()));
                cfg.setProviderName(saml.getProviderName());
                cfg.setNameIdPolicyAllowCreate(saml.getNameIdPolicyAllowCreate().toBoolean());

                if (StringUtils.isNotBlank(saml.getSaml2AttributeConverter())) {
                    if (ScriptingUtils.isExternalGroovyScript(saml.getSaml2AttributeConverter())) {
                        FunctionUtils.doAndHandle(__ -> {
                            val resource = ResourceUtils.getResourceFrom(saml.getSaml2AttributeConverter());
                            val script = new WatchableGroovyScriptResource(resource);
                            cfg.setSamlAttributeConverter(new GroovyAttributeConverter(script));
                        });
                    } else {
                        FunctionUtils.doAndHandle(__ -> {
                            val clazz = ClassUtils.getClass(getClass().getClassLoader(), saml.getSaml2AttributeConverter());
                            val converter = (AttributeConverter) clazz.getDeclaredConstructor().newInstance();
                            cfg.setSamlAttributeConverter(converter);
                        });
                    }
                }

                val mappedAttributes = saml.getMappedAttributes();
                if (!mappedAttributes.isEmpty()) {
                    cfg.setMappedAttributes(CollectionUtils.convertDirectedListToMap(mappedAttributes));
                }

                val client = new SAML2Client(cfg);
                LOGGER.debug("Created SAML2 delegated client [{}]", client);
                return new ConfigurableDelegatedClient(client, saml);
            })
            .collect(Collectors.toList());
    }
}
