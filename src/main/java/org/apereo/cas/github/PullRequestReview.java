package org.apereo.cas.github;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(of = {"id", "state", "user"}, includeFieldNames = false)
public class PullRequestReview {
    private long id;
    private User user;
    private String body;
    private ReviewStatus state;
    private String author_association;

    public enum ReviewStatus {
        DISMISSED,
        APPROVED,
        CHANGES_REQUESTED
    }
}
