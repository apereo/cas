/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.cas;

import org.apereo.cas.github.GitHubOperations;
import org.apereo.cas.github.Label;
import org.apereo.cas.github.Milestone;
import org.apereo.cas.github.Page;
import org.apereo.cas.github.PullRequest;

import com.github.zafarkhaja.semver.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * A repository that should be monitored.
 *
 * @author Andy Wilkinson
 */
@RequiredArgsConstructor
@Getter
public class MonitoredRepository implements InitializingBean {
    private final GitHubOperations gitHub;
    private final GitHubProperties gitHubProperties;

    private final List<Milestone> milestones = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();

    private Version currentVersionInMaster;

    @Override
    public void afterPropertiesSet() throws Exception {
        final RestTemplate rest = new RestTemplate();
        final URI uri = URI.create(gitHubProperties.getRepository().getUrl() + "/raw/master/gradle.properties");
        final ResponseEntity entity = rest.getForEntity(uri, String.class);
        final Properties properties = new Properties();
        properties.load(new StringReader(entity.getBody().toString()));
        currentVersionInMaster = Version.valueOf(properties.get("version").toString());

        Page<Milestone> page = gitHub.getMilestones(getOrganization(), getName());
        while (page != null) {
            milestones.addAll(page.getContent());
            page = page.next();
        }

        Page<Label> lbl = gitHub.getLabels(getOrganization(), getName());
        while (lbl != null) {
            labels.addAll(lbl.getContent());
            lbl = lbl.next();
        }
    }

    public String getOrganization() {
        return this.gitHubProperties.getRepository().getOrganization();
    }

    public String getName() {
        return this.gitHubProperties.getRepository().getName();
    }

    public Optional<Milestone> getMilestoneForMaster() {
        final String currentVersion = currentVersionInMaster.toString().replace("-SNAPSHOT", "");
        return milestones.stream()
            .filter(milestone -> {
                final String milestoneVersion = Version.valueOf(milestone.getTitle()).toString();
                return milestoneVersion.equalsIgnoreCase(currentVersion);
            })
            .findFirst();
    }

    public Optional<Milestone> getMilestoneForBranch(final String branch) {
        final Version branchVersion = Version.valueOf(branch.replace(".x", "." + Integer.MAX_VALUE));
        return milestones.stream()
            .filter(milestone -> {
                final Version milestoneVersion = Version.valueOf(milestone.getTitle());
                return milestoneVersion.getMajorVersion() == branchVersion.getMajorVersion()
                    && milestoneVersion.getMinorVersion() == branchVersion.getMinorVersion();
            })
            .findFirst();
    }

    public PullRequest mergePullRequestWithMaster(final PullRequest pr) {
        return this.gitHub.mergeWithHead(getOrganization(), getName(), pr);
    }

    private static Predicate<Label> getLabelPredicateByName(final CasLabels name) {
        return l -> l.getName().contains(name.getTitle());
    }

    public void labelPullRequestAs(final PullRequest pr, final CasLabels labelName) {
        this.labels.stream().filter(getLabelPredicateByName(labelName)).findFirst().ifPresent(l -> {
            this.gitHub.addLabel(pr, l.getName());
        });
    }
}
