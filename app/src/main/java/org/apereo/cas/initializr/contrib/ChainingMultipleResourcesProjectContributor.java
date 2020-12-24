package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.jooq.lambda.Unchecked;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChainingMultipleResourcesProjectContributor implements ProjectContributor {
    private final List<MultipleResourcesProjectContributor> contributors = new ArrayList<>();

    public void addContributor(MultipleResourcesProjectContributor c) {
        contributors.add(c);
    }

    @Override
    public void contribute(final Path projectRoot) {
        this.contributors.stream()
            .sorted(Comparator.comparing(MultipleResourcesProjectContributor::getOrder))
            .forEach(Unchecked.consumer(c -> c.contribute(projectRoot)));
    }
}
