package org.apereo.cas.shell.commands;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.idp.metadata.DefaultSamlIdPMetadataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * This is {@link GenerateSamlIdPMetadataCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Service
@Slf4j
public class GenerateSamlIdPMetadataCommand implements CommandMarker {
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
    @CliCommand(value = "generate-idp-metadata", help = "Generate SAML2 IdP Metadata")
    public void generate(
        @CliOption(key = {"metadataLocation"},
            help = "Directory location to hold metadata and relevant keys/certificates",
            specifiedDefaultValue = "/etc/cas/saml",
            unspecifiedDefaultValue = "/etc/cas/saml",
            optionContext = "Directory location to hold metadata and relevant keys/certificates") final String metadataLocation,
        @CliOption(key = {"entityId"},
            help = "Entity ID to use for the generated metadata",
            specifiedDefaultValue = "cas.example.org",
            unspecifiedDefaultValue = "cas.example.org",
            optionContext = "Entity ID to use for the generated metadata") final String entityId,
        @CliOption(key = {"hostName"},
            help = "CAS server prefix to be used at the IdP host name when generating metadata",
            specifiedDefaultValue = "https://cas.example.org/cas",
            unspecifiedDefaultValue = "https://cas.example.org/cas",
            optionContext = "CAS server prefix to be used at the IdP host name when generating metadata") final String serverPrefix,
        @CliOption(key = {"scope"},
            help = "Scope to use when generating metadata",
            specifiedDefaultValue = "example.org",
            unspecifiedDefaultValue = "example.org",
            optionContext = "Scope to use when generating metadata") final String scope,
        @CliOption(key = {"force"},
            specifiedDefaultValue = "false",
            unspecifiedDefaultValue = "false",
            help = "Force metadata generation, disregarding anything that might already be available at the specified location",
            optionContext = "Force metadata generation, disregarding anything that might already be available at the specified location") final boolean force) {

        final DefaultSamlIdPMetadataGenerator generator = new DefaultSamlIdPMetadataGenerator(new File(metadataLocation),
            entityId, this.resourceLoader, serverPrefix, scope);

        boolean generateMetadata = true;
        if (!generator.isMetadataMissing()) {
            LOGGER.warn("Metadata artifacts are available at the specified location: [{}]", metadataLocation);
            generateMetadata = force;
        }
        if (generateMetadata) {
            generator.initialize();
            final File file = generator.generate();
            LOGGER.info("Generated metadata is available at [{}]", file);
        } else {
            LOGGER.info("No metadata was generated; it might already exist at the specified path");
        }
    }
}
