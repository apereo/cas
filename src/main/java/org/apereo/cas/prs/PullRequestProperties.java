package org.apereo.cas.prs;

import org.apereo.cas.github.Label;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This is {@link PullRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
public class PullRequestProperties {
    private List<String> maintainedBranches;

}
