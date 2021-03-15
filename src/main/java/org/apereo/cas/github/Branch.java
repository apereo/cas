package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = "name")
public class Branch {

    private final String name;
    private final Commit commit;

    @JsonCreator
    public Branch(@JsonProperty("name") final String name,
                        @JsonProperty("commit") final Commit commit) {
        this.name = name;
        this.commit = commit;
    }
}
