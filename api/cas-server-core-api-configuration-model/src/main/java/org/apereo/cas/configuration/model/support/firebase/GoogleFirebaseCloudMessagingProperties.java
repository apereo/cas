package org.apereo.cas.configuration.model.support.firebase;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link GoogleFirebaseCloudMessagingProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-notifications-fcm")
@Accessors(chain = true)
public class GoogleFirebaseCloudMessagingProperties implements Serializable {
    private static final long serialVersionUID = -5679682641899738092L;

    /**
     * The principal attribute name
     * that contains the registration token for the user.
     * Registration tokens that are provided by clients during the handshake process
     * should be stored on the server, and made available to CAS
     * as a principal attribute.
     */
    @RequiredProperty
    private String registrationTokenAttributeName;

    /**
     * Path to the service account key json file.
     * This can optional if you set the environment variable {@code GOOGLE_APPLICATION_CREDENTIALS} to the file path
     * of the JSON file that contains your service account key. If this is undefined, the property value will be used instead.
     */
    private SpringResourceProperties serviceAccountKey;

    /**
     * Firebase database url.
     */
    private String databaseUrl;

    /**
     * Required scopes to properly communicate with the firebase cloud.
     */
    private List<String> scopes = Stream.of("https://www.googleapis.com/auth/firebase.messaging").collect(Collectors.toList());
}
