package org.apereo.cas.support.inwebo.service.response;

import lombok.Getter;
import lombok.Setter;

/**
 * The abstract JSON response.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Getter
@Setter
public abstract class AbstractInweboResponse {

    private InweboResult result;

    public boolean isOk() {
        return result == InweboResult.OK;
    }
}
