package org.apereo.cas.adaptors.duo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This is {@link DuoIntegration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DuoIntegration {

    /**
     * The enum Duo enrollment policy status.
     */
    public enum DuoEnrollmentPolicyStatus {
        /**
         * Enroll duo enrollment policy status.
         */
        ENROLL,
        /**
         * Allow duo enrollment policy status.
         */
        ALLOW,
        /**
         * Deny duo enrollment policy status.
         */
        DENY
    }

    private DuoEnrollmentPolicyStatus enrollmentPolicyStatus;

    private String greeting;

    private String name;

    private String type;

    public DuoIntegration(final String name) {
        this.name = name;
    }

    public DuoEnrollmentPolicyStatus getEnrollmentPolicyStatus() {
        return enrollmentPolicyStatus;
    }

    /**
     * Sets enrollment policy status.
     *
     * @param enrollmentPolicyStatus the enrollment policy status
     * @return the integration
     */
    public DuoIntegration setEnrollmentPolicyStatus(final DuoEnrollmentPolicyStatus enrollmentPolicyStatus) {
        this.enrollmentPolicyStatus = enrollmentPolicyStatus;
        return this;
    }

    public String getGreeting() {
        return greeting;
    }

    /**
     * Sets greeting.
     *
     * @param greeting the greeting
     * @return the integration
     */
    public DuoIntegration setGreeting(final String greeting) {
        this.greeting = greeting;
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     * @return the integration
     */
    public DuoIntegration setName(final String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     * @return the integration
     */
    public DuoIntegration setType(final String type) {
        this.type = type;
        return this;
    }

    /**
     * Is enrollment status bypass boolean.
     *
     * @return the boolean
     */
    public boolean isEnrollmentStatusBypass() {
        return this.enrollmentPolicyStatus == DuoEnrollmentPolicyStatus.ALLOW;
    }

    /**
     * New instance duo user enrollment policy.
     *
     * @param name the name
     * @return the duo user enrollment policy
     */
    public static DuoIntegration newInstance(final String name) {
        return new DuoIntegration(name);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("enrollmentPolicyStatus", enrollmentPolicyStatus)
                .append("greeting", greeting)
                .append("name", name)
                .append("type", type)
                .toString();
    }
}
