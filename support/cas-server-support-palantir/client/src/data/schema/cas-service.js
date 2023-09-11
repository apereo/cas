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
        /*{
            "type": "Category",
            "label": "Delegated Authentication",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/accessStrategy/properties/delegatedAuthenticationPolicy"
                        }
                    ]
                }
            ]
        },*/
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
            "label": "Multifactor",
            "elements": [
                {
                    "type": "VerticalLayout",
                    "elements": [
                        {
                            "type": "Control",
                            "scope": "#/properties/multifactorAuthenticationPolicy"
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
                        }
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