package org.apereo.cas.initializr.contrib;

import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.jooq.lambda.Unchecked;
import org.springframework.core.Ordered;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChainingSingleResourceProjectContributor implements ProjectContributor {
    private final List<ProjectContributor> contributors = new ArrayList<>();

    private int order = Ordered.LOWEST_PRECEDENCE;

    public void addContributor(ProjectContributor c) {
        contributors.add(c);
    }

    @Override
    public void contribute(final Path projectRoot) {
        this.contributors.stream()
            .sorted(Comparator.comparing(ProjectContributor::getOrder))
            .forEach(Unchecked.consumer(c -> c.contribute(projectRoot)));
    }

    @Override
    public int getOrder() {
        return order;
    }
}
