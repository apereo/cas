package org.apereo.cas.shell.commands.saml;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.DefaultSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;

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
     */
    @ShellMethod(key = "generate-idp-metadata", value = "Generate SAML2 IdP Metadata")
    public void generate(
        @ShellOption(value = {"metadataLocation"},
            help = "Directory location to hold metadata and relevant keys/certificates",
            defaultValue = "/etc/cas/saml") final String metadataLocation,
        @ShellOption(value = {"entityId"},
            help = "Entity ID to use for the generated metadata",
            defaultValue = "cas.example.org") final String entityId,
        @ShellOption(value = {"hostName"},
            help = "CAS server prefix to be used at the IdP host name when generating metadata",
            defaultValue = "https://cas.example.org/cas") final String serverPrefix,
        @ShellOption(value = {"scope"},
            help = "Scope to use when generating metadata",
            defaultValue = "example.org") final String scope,
        @ShellOption(value = {"force"},
            help = "Force metadata generation, disregarding anything that might already be available at the specified location") final boolean force) {

        final SamlIdPMetadataLocator locator = new DefaultSamlIdPMetadataLocator(new File(metadataLocation));
        final var writer = new DefaultSamlIdPCertificateAndKeyWriter();
        final var generator = new FileSystemSamlIdPMetadataGenerator(entityId, this.resourceLoader,
            serverPrefix, scope, locator, writer);

        var generateMetadata = true;
        if (!locator.exists()) {
            LOGGER.warn("Metadata artifacts are available at the specified location: [{}]", metadataLocation);
            generateMetadata = force;
        }
        if (generateMetadata) {
            generator.initialize();
            generator.generate();
            LOGGER.info("Generated metadata is available at [{}]", locator.getMetadata());
        } else {
            LOGGER.info("No metadata was generated; it might already exist at the specified path");
        }
    }
}
