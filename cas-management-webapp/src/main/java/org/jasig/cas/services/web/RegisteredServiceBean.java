/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services.web;

import org.jasig.cas.services.AbstractAttributeReleasePolicy;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.ReturnMappedAttributeReleasePolicy;

import java.io.Serializable;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceBean implements Serializable {

    private static final long serialVersionUID = 4882440567964605644L;

    private long assignedId;
    private boolean sasCASEnabled;
    private String serviceId;
    private String name;
    private String description;
    private String logoUrl;
    private ProxyPolicy proxyPolicy = new ProxyPolicy();
    private AttributeReleasePolicy attrRelease = new AttributeReleasePolicy();

    public long getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(final long assignedId) {
        this.assignedId = assignedId;
    }

    public boolean isSasCASEnabled() {
        return sasCASEnabled;
    }

    public void setSasCASEnabled(final boolean sasCASEnabled) {
        this.sasCASEnabled = sasCASEnabled;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public ProxyPolicy getProxyPolicy() {
        return proxyPolicy;
    }

    public void setProxyPolicy(final ProxyPolicy proxyPolicy) {
        this.proxyPolicy = proxyPolicy;
    }

    public AttributeReleasePolicy getAttrRelease() {
        return attrRelease;
    }

    public void setAttrRelease(final AttributeReleasePolicy attrRelease) {
        this.attrRelease = attrRelease;
    }

    /**
     * The type Attribute release policy.
     */
    public static class AttributeReleasePolicy {
        private boolean releasePassword;
        private boolean releaseTicket;
        private ReleasePolicyStrategy attrPolicy = new ReleasePolicyStrategy();

        /**
         * The type Release policy strategy.
         */
        public static class ReleasePolicyStrategy {

            /**
             * The enum Types.
             */
            public enum Types {
                /** Refuse type. */
                ALL("all"),

                /** Mapped type. */
                MAPPED("mapped"),

                /** None type. */
                NONE("none"),

                /** Allow type. */
                ALLOWED("allowed");

                private final String value;

                /**
                 * Instantiates a new Types.
                 *
                 * @param value the value
                 */
                Types(final String value) {
                    this.value = value;
                }
            }

            private String type;

            public String getType() {
                return type;
            }

            public void setType(final String type) {
                this.type = type;
            }
        }

        public boolean isReleasePassword() {
            return releasePassword;
        }

        public void setReleasePassword(final boolean releasePassword) {
            this.releasePassword = releasePassword;
        }

        public boolean isReleaseTicket() {
            return releaseTicket;
        }

        public void setReleaseTicket(final boolean releaseTicket) {
            this.releaseTicket = releaseTicket;
        }

        public ReleasePolicyStrategy getAttrPolicy() {
            return attrPolicy;
        }

        public void setAttrPolicy(final ReleasePolicyStrategy attrPolicy) {
            this.attrPolicy = attrPolicy;
        }
    }

    /**
     * The type Proxy policy.
     */
    public static class ProxyPolicy {

        /**
         * The enum Types.
         */
        public enum Types {
            /** Refuse type. */
            REFUSE("refuse"),

            /** Allow type. */
            ALLOW("allow");

            private final String value;

            /**
             * Instantiates a new Types.
             *
             * @param value the value
             */
            Types(final String value) {
                this.value = value;
            }
        }
        private String type;
        private String value;

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }

    /**
     * From registered service to a service bean.
     *
     * @param svc the svc
     * @return the registered service bean
     */
    public static RegisteredServiceBean fromRegisteredService(final RegisteredService svc) {
        final RegisteredServiceBean bean = new RegisteredServiceBean();
        bean.setAssignedId(svc.getId());
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toExternalForm());
        }

        final RegisteredServiceProxyPolicy policy = svc.getProxyPolicy();
        final ProxyPolicy proxyPolicyBean = bean.getProxyPolicy();

        if (policy instanceof RefuseRegisteredServiceProxyPolicy) {
            final RefuseRegisteredServiceProxyPolicy refuse = (RefuseRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(ProxyPolicy.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy option = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(ProxyPolicy.Types.ALLOW.toString());
            proxyPolicyBean.setValue(option.getPattern().toString());
        }

        final AbstractAttributeReleasePolicy attrPolicy = (AbstractAttributeReleasePolicy) svc.getAttributeReleasePolicy();
        final AttributeReleasePolicy attrPolicyBean = bean.getAttrRelease();

        attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
        attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());

        if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
            attrPolicyBean.getAttrPolicy().setType(
                    AttributeReleasePolicy.ReleasePolicyStrategy.Types.ALL.toString());
        } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
            final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
            if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                attrPolicyBean.getAttrPolicy().setType(
                        AttributeReleasePolicy.ReleasePolicyStrategy.Types.NONE.toString());
            } else {
                attrPolicyBean.getAttrPolicy().setType(
                        AttributeReleasePolicy.ReleasePolicyStrategy.Types.ALLOWED.toString());
            }
        } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
            final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
            if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                attrPolicyBean.getAttrPolicy().setType(
                        AttributeReleasePolicy.ReleasePolicyStrategy.Types.NONE.toString());
            } else {
                attrPolicyBean.getAttrPolicy().setType(
                        AttributeReleasePolicy.ReleasePolicyStrategy.Types.MAPPED.toString());
            }
        }
        bean.setSasCASEnabled(svc.getAccessStrategy().isServiceAccessAllowed());
        return bean;
    }
}
