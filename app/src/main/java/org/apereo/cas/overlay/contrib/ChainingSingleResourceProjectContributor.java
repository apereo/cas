package org.apereo.cas.overlay.contrib;

import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.jooq.lambda.Unchecked;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChainingSingleResourceProjectContributor implements ProjectContributor {
    private final List<ProjectContributor> contributors = new ArrayList<>();

    public void addContributor(ProjectContributor c) {
        contributors.add(c);
    }

    @Override
    public void contribute(final Path projectRoot) {
        this.contributors.stream()
            .sorted(Comparator.comparing(ProjectContributor::getOrder))
            .forEach(Unchecked.consumer(c -> c.contribute(projectRoot)));
    }
}
