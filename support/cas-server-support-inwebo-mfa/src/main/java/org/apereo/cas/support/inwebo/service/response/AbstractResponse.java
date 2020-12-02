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
public abstract class AbstractResponse {

    private Result result;

    public boolean isOk() {
        return result == Result.OK;
    }
}
