package org.jasig.cas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 * This is {@link HazelcastHttpSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration
@EnableHazelcastHttpSession
public class HazelcastHttpSessionConfiguration {
}
