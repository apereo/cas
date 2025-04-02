package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link TenantEmailCommunicationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TenantEmailCommunicationPolicy implements Serializable {
    @Serial
    private static final long serialVersionUID = 8552529921936294263L;

    private String from;
}
