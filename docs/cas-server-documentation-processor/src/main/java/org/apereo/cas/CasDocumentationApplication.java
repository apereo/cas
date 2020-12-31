package org.apereo.cas;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;

import java.io.File;
import java.util.List;

/**
 * This is {@link CasDocumentationApplication}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class CasDocumentationApplication {
    public static void main(final String[] args) throws Exception {
        var results = CasConfigurationMetadataRepository.query(
            ConfigurationMetadataCatalogQuery.builder()
                .modules(List.of("cas-server-support-captcha"))
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .build());
        var f = new File("/Users/Misagh/Workspace/GitWorkspace/cas-server/docs/cas-server-documentation-processor/src/main/java/org/apereo/cas/file.yml");

        results.export(f);
    }
}
