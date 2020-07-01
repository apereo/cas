package org.apache.syncope.common.lib;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link Attr}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Accessors(chain = true)
public class Attr implements Serializable {
    private static final long serialVersionUID = 7791304495192615740L;

    private String schema;

    private List<?> values = new ArrayList<>();
}
