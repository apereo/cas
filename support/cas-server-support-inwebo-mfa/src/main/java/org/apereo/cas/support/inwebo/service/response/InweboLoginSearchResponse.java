package org.apereo.cas.support.inwebo.service.response;

import lombok.Getter;
import lombok.Setter;

/**
 * The JSON response with the user information.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Getter
@Setter
public class InweboLoginSearchResponse extends AbstractInweboResponse {

    private long count;

    private long userId;

    private long userStatus;

    private long activationStatus;
}
