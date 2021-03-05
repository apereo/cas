package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.git.GitRepository;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;

import java.util.Optional;

/**
 * This is {@link GitSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
public class GitSamlIdPMetadataGenerator extends FileSystemSamlIdPMetadataGenerator implements InitializingBean {
    private final GitRepository gitRepository;

    public GitSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                       final GitRepository gitRepository) {
        super(context);
        this.gitRepository = gitRepository;
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = SamlIdPMetadataGenerator.getAppliesToFor(registeredService);
        doc.setAppliesTo(appliesTo);
        gitRepository.commitAll("Generated metadata for " + appliesTo);

        val props = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getMetadata().getGit();
        if (props.isPushChanges()) {
            gitRepository.push();
        }
        return super.finalizeMetadataDocument(doc, registeredService);
    }
}
