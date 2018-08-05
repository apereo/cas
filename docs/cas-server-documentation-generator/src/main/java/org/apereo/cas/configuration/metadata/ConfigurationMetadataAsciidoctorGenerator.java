package org.apereo.cas.configuration.metadata;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;

import lombok.val;
import org.springframework.core.io.UrlResource;

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
        val r = new UrlResource("jar:file:/Users/Misagh/Workspace/GitWorkspace/cas-server/api/cas-server-core-api-configuration-model/build/libs/cas-server-core-api-configuration-model-6.0.0-RC2-SNAPSHOT.jar!/META-INF/spring-configuration-metadata.json");
        
        val repository = new CasConfigurationMetadataRepository(r).getRepository();
        val groups = repository.getAllGroups();
        //val properties = repository.getAllProperties();

        groups.entrySet().forEach(e -> {
            if (e.getValue().getProperties().isEmpty() && e.getKey().startsWith("cas.")) {
                System.out.println(e.getValue().getId());
                System.out.println("Sources:" + e.getValue().getSources().keySet());
                System.out.println("Properties: " + e.getValue().getProperties().size());
                System.out.println("-----------------------");
            }
        });

//        val pp = groups.get("cas.service-registry.sms");
//        System.out.println(pp.getProperties());

//        val prop = properties.get("cas.authn.mfa.u2f.groovy.location");
//        System.out.println(prop.getName());
//        System.out.println(prop.getId());
//        System.out.println(prop.getDescription());
//        System.out.println(prop.getDefaultValue());
        //System.out.println(properties.size());
    }
}
