package org.apereo.cas.configuration.metadata;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;

import lombok.val;

/**
 * This is {@link ConfigurationMetadataAsciidoctorGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ConfigurationMetadataAsciidoctorGenerator {

    /**
     * Main.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        new ConfigurationMetadataAsciidoctorGenerator().execute();
    }

    /**
     * Load configuration properties and generate .adoc files.
     *
     * @throws Exception the exception
     */
    public void execute() throws Exception {
        val repository = new CasConfigurationMetadataRepository().getRepository();
        val properties = repository.getAllProperties();
        val groups = repository.getAllGroups();
    }
}
