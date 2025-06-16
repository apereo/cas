package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = {"id", "name", "state"}, includeFieldNames = false)
public class Workflow {
    private final String id;
    private final String name;
    private final String path;
    private final String state;

    @JsonCreator
    public Workflow(@JsonProperty("id") final String id,
                    @JsonProperty("name") final String name,
                    @JsonProperty("path") final String path,
                    @JsonProperty("state") final String state) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.state = state;
    }
}
