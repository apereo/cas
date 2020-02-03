package org.apereo.cas.shell.commands.saml;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * This is {@link GenerateSamlIdPMetadataCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ShellCommandGroup("SAML")
@ShellComponent
@Slf4j
public class GenerateSamlIdPMetadataCommand {
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Generate saml2 idp metadata at the specified location.
     *
     * @param metadataLocation the metadata location
     * @param entityId         the entity id
     * @param serverPrefix     the server prefix
     * @param scope            the scope
     * @param force            force generation of metadata
     * @param subjectAltNames  additional subject alternative names for cert (besides entity id)
     */
    @ShellMethod(key = "generate-idp-metadata", value = "Generate SAML2 IdP Metadata")
    public void generate(
        @ShellOption(value = {"metadataLocation", "--metadataLocation"},
            help = "Directory location to hold metadata and relevant keys/certificates",
            defaultValue = "/etc/cas/saml") final String metadataLocation,
        @ShellOption(value = {"entityId", "--entityId"},
            help = "Entity ID to use for the generated metadata",
            defaultValue = "cas.example.org") final String entityId,
        @ShellOption(value = {"hostName", "--hostName"},
            help = "CAS server prefix to be used at the IdP host name when generating metadata",
            defaultValue = "https://cas.example.org/cas") final String serverPrefix,
        @ShellOption(value = {"scope", "--scope"},
            help = "Scope to use when generating metadata",
            defaultValue = "example.org") final String scope,
        @ShellOption(value = {"force", "--force"},
            help = "Force metadata generation (XML only, not certs), overwriting anything at the specified location") final boolean force,
        @ShellOption(value = {"subjectAltNames", "--subjectAltNames"},
            help = "Comma separated list of other subject alternative names for the certificate (besides entityId)",
            defaultValue = StringUtils.EMPTY) final String subjectAltNames) {

        val locator = new FileSystemSamlIdPMetadataLocator(new File(metadataLocation));
        val writer = new DefaultSamlIdPCertificateAndKeyWriter();
        writer.setHostname(entityId);
        if (StringUtils.isNotBlank(subjectAltNames)) {
            writer.setUriSubjectAltNames(Arrays.asList(StringUtils.split(subjectAltNames, ",")));
        }

        val generateMetadata = FunctionUtils.doIf(locator.exists(Optional.empty()),
            () -> Boolean.TRUE,
            () -> {
                LOGGER.warn("Metadata artifacts are available at the specified location [{}]", metadataLocation);
                return force;
            }).get();

        if (generateMetadata) {
            val props = new CasConfigurationProperties();
            props.getAuthn().getSamlIdp().setEntityId(entityId);
            props.getServer().setScope(scope);
            props.getServer().setPrefix(serverPrefix);

            val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
                .samlIdPMetadataLocator(locator)
                .samlIdPCertificateAndKeyWriter(writer)
                .resourceLoader(resourceLoader)
                .casProperties(props)
                .metadataCipherExecutor(CipherExecutor.noOpOfStringToString())
                .build();

            val generator = new FileSystemSamlIdPMetadataGenerator(context);
            generator.initialize();
            generator.generate(Optional.empty());
            LOGGER.info("Generated metadata is available at [{}]", locator.resolveMetadata(Optional.empty()));
        } else {
            LOGGER.info("No metadata was generated; it might already exist at the specified path");
        }
    }
}
