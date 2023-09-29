export const casServiceUiSchema = {
    "type": "Categorization",
    "elements": [
        {
            "type": "Category",
            "label": "Basics",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Group",
                            "label": "Basics",
                            "elements": [
                                {
                                    "type": "Control",
                                    "scope": "#/properties/name"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/serviceId"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/evaluationOrder"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/description"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/theme"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/templateName"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/locale"
                                },
                            ]
                        },
                        {
                            "type": "Group",
                            "label": "URLs",
                            "elements": [
                                {
                                    "type": "Control",
                                    "scope": "#/properties/logo"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/informationUrl"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/privacyUrl"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/redirectUrl"
                                },
                            ]
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Contacts",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/contacts",
                            "options": {
                                "elementLabelProp": "name",
                                "detail": {
                                    "type": "VerticalLayout",
                                    "elements": [
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/name"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/email"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/department"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/phone"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/type"
                                        },
                                    ]
                                }
                            }
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Logout",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Group",
                            "elements": [
                                {
                                    "type": "Control",
                                    "scope": "#/properties/logoutUrl"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/logoutType"
                                }
                            ]
                        },

                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Attribute Release",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/attributeReleasePolicy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Acceptable Usage Policy",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/acceptableUsagePolicy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Access Strategy",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/accessStrategy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Authentication Policy",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/authenticationPolicy",
                            "options": {
                                "elementLabelProp": "name",
                                "detail": {
                                    "type": "VerticalLayout",
                                    "elements": [
                                        {
                                            "type": "Control",
                                            "label": "Criteria",
                                            "scope": "#/properties/criteria"
                                        },
                                        {
                                            "type": "Group",
                                            "label": "Required Authentication Handlers",
                                            "elements": [
                                                {
                                                    "type": "Control",
                                                    "scope": "#/properties/requiredAuthenticationHandlers"
                                                }
                                            ]
                                        },
                                        {
                                            "type": "Group",
                                            "label": "Excluded Authentication Handlers",
                                            "elements": [
                                                {
                                                    "type": "Control",
                                                    "scope": "#/properties/excludedAuthenticationHandlers"
                                                }
                                            ]
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/@class"
                                        },
                                    ]
                                }
                            }
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Matching Strategy",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/matchingStrategy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Multifactor Authentication",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/multifactorPolicy",
                            "options": {
                                "elementLabelProp": "name",
                                "detail": {
                                    "type": "VerticalLayout",
                                    "elements": [
                                        {
                                            "type": "Group",
                                            "label": "Multifactor Authentication Providers",
                                            "elements": [
                                                {
                                                    "type": "Control",
                                                    "scope": "#/properties/multifactorAuthenticationProviders"
                                                }
                                            ]
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/bypassEnabled"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/bypassIfMissingPrincipalAttribute"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/bypassPrincipalAttributeName"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/bypassPrincipalAttributeValue"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/bypassTrustedDeviceEnabled"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/failureMode"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/forceExecution"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/principalAttributeNameTrigger"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/principalAttributeValueToMatch"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/script"
                                        },
                                        {
                                            "type": "Control",
                                            "scope": "#/properties/@class"
                                        }
                                    ]
                                }
                            }
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "SSO Policy",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/singleSignOnParticipationPolicy"
                        }
                    ]
                }
            ]
        },

        {
            "type": "Category",
            "label": "Tickets",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/ticketGrantingTicketExpirationPolicy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Username Attribute",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/usernameAttributeProvider"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Webflow Interrupt",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/webflowInterruptPolicy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Proxy",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/proxyPolicy"
                        },
                        {
                            "type": "Control",
                            "scope": "#/properties/proxyTicketExpirationPolicy"
                        },
                        {
                            "type": "Control",
                            "scope": "#/properties/proxyGrantingTicketExpirationPolicy"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Service Ticket Expiration",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/serviceTicketExpirationPolicy"
                        },
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Properties",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/properties"
                        }
                    ]
                }
            ]
        },
        {
            "type": "Category",
            "label": "Advanced",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Group",
                            "elements": [
                                {
                                    "type": "Control",
                                    "scope": "#/properties/evaluationOrder"
                                },
                                {
                                    "type": "Control",
                                    "scope": "#/properties/responseType"
                                }
                            ]
                        },
                        {
                            "type": "Group",
                            "label": "Environments",
                            "elements": [
                                {
                                    "type": "Control",
                                    "scope": "#/properties/environments"
                                },
                            ]
                        },
                        {
                            "type": "Group",
                            "label": "Supported Protocols",
                            "elements": [
                                {
                                    "type": "Control",
                                    "scope": "#/properties/supportedProtocols"
                                },
                            ]
                        },
                        {
                            "type": "Control",
                            "scope": "#/properties/expirationPolicy"
                        },
                        {
                            "type": "Control",
                            "scope": "#/properties/publicKey"
                        }
                    ]
                }
            ]
        }
    ]
};

export default casServiceUiSchema;
