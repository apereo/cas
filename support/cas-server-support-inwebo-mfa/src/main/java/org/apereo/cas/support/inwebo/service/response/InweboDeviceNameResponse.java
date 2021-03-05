package org.apereo.cas.support.inwebo.service.response;

import lombok.Getter;
import lombok.Setter;

/**
 * The JSON response with the device name.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Getter
@Setter
public class InweboDeviceNameResponse extends AbstractInweboResponse {

    private String deviceName;
}
