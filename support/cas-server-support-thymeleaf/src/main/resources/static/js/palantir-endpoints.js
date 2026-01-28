class CasActuatorEndpoints {
    static health() {
        return actuatorEndpoints.health;
    }

    static discoveryProfile() {
        return actuatorEndpoints.discoveryprofile || actuatorEndpoints.discoveryProfile;
    }

    static casConfig() {
        return actuatorEndpoints.casconfig || actuatorEndpoints.casConfig;
    }

    static env() {
        return actuatorEndpoints.env;
    }

    static info() {
        return actuatorEndpoints.info;
    }

    static throttles() {
        return actuatorEndpoints.throttles;
    }

    static statistics() {
        return actuatorEndpoints.statistics;
    }

    static metrics() {
        return actuatorEndpoints.metrics;
    }

    static auditEvents() {
        return actuatorEndpoints.auditevents || actuatorEndpoints.auditEvents;
    }

    static personDirectory() {
        return actuatorEndpoints.persondirectory || actuatorEndpoints.personDirectory;
    }

    static configProps() {
        return actuatorEndpoints.configprops || actuatorEndpoints.configProps;
    }

    static mfaDevices() {
        return actuatorEndpoints.mfadevices || actuatorEndpoints.mfaDevices;
    }

    static refresh() {
        return actuatorEndpoints.busrefresh || actuatorEndpoints.refresh;
    }

    static shutdown() {
        return actuatorEndpoints.shutdown;
    }

    static restart() {
        return actuatorEndpoints.restart;
    }

    static configurationMetadata() {
        return actuatorEndpoints.configurationmetadata || actuatorEndpoints.configurationMetadata;
    }

    static attributeConsent() {
        return actuatorEndpoints.attributeconsent || actuatorEndpoints.attributeConsent;
    }

    static httpExchanges() {
        return actuatorEndpoints.httpexchanges || actuatorEndpoints.httpExchanges;
    }

    static serviceAccess() {
        return actuatorEndpoints.serviceaccess || actuatorEndpoints.serviceAccess;
    }

    static springWebflow() {
        return actuatorEndpoints.springwebflow || actuatorEndpoints.springWebflow;
    }

    static events() {
        return actuatorEndpoints.events;
    }

    static auditLog() {
        return actuatorEndpoints.auditlog || actuatorEndpoints.auditLog;
    }

    static casFeatures() {
        return actuatorEndpoints.casfeatures || actuatorEndpoints.casFeatures;
    }

    static loggers() {
        return actuatorEndpoints.loggers;
    }

    static loggingConfig() {
        return actuatorEndpoints.loggingconfig || actuatorEndpoints.loggingConfig;
    }

    static ticketRegistry() {
        return actuatorEndpoints.ticketregistry || actuatorEndpoints.ticketRegistry;
    }

    static heapDump() {
        return actuatorEndpoints.heapdump || actuatorEndpoints.heapDump;
    }

    static sessions() {
        return actuatorEndpoints.sessions;
    }

    static ssoSessions() {
        return actuatorEndpoints.ssosessions || actuatorEndpoints.ssoSessions;
    }

    static registeredServices() {
        return actuatorEndpoints.registeredservices || actuatorEndpoints.registeredServices;
    }

    static oidcJwks() {
        return actuatorEndpoints.oidcjwks || actuatorEndpoints.oidcJwks;
    }

    static heimdall() {
        return actuatorEndpoints.heimdall;
    }

    static logFile() {
        return actuatorEndpoints.logfile || actuatorEndpoints.logFile;
    }

    static scheduledTasks() {
        return actuatorEndpoints.scheduledtasks || actuatorEndpoints.scheduledTasks;
    }

    static multifactorTrustedDevices() {
        return actuatorEndpoints.multifactoreddevices || actuatorEndpoints.multifactorTrustedDevices;
    }

    static multitenancy() {
        return actuatorEndpoints.multitenancy;
    }

    static samlPostProfileResponse() {
        return actuatorEndpoints.samlpostprofileresponse || actuatorEndpoints.samlPostProfileResponse;
    }

    static casValidate() {
        return actuatorEndpoints.casvalidate || actuatorEndpoints.casValidate;
    }

    static samlValidate() {
        return actuatorEndpoints.samlvalidate || actuatorEndpoints.samlValidate;
    }

    static gcpLogs() {
        return actuatorEndpoints.gcplogs || actuatorEndpoints.gcpLogs;
    }

    static cloudWatchLogs() {
        return actuatorEndpoints.cloudwatchlogs || actuatorEndpoints.cloudWatchLogs;
    }

    static attributeDefinitions() {
        return actuatorEndpoints.attributedefinitions || actuatorEndpoints.attributeDefinitions;
    }

    static delegatedClients() {
        return actuatorEndpoints.delegatedclients || actuatorEndpoints.delegatedClients;
    }

    static authenticationHandlers() {
        return actuatorEndpoints.authenticationhandlers || actuatorEndpoints.authenticationHandlers;
    }

    static ticketExpirationPolicies() {
        return actuatorEndpoints.ticketexpirationpolicies || actuatorEndpoints.ticketExpirationPolicies;
    }

    static authenticationPolicies() {
        return actuatorEndpoints.authenticationpolicies || actuatorEndpoints.authenticationPolicies;
    }

    static entityHistory() {
        return actuatorEndpoints.entityhistory || actuatorEndpoints.entityHistory;
    }

    static samlIdpRegisteredServiceMetadataCache() {
        return actuatorEndpoints.samlidpregisteredservicemetadatacache || actuatorEndpoints.samlIdpRegisteredServiceMetadataCache;
    }

    static threadDump() {
        return actuatorEndpoints.threaddump || actuatorEndpoints.threadDump;
    }

    static all() {
        return Object.getOwnPropertyNames(CasActuatorEndpoints)
            .filter(name =>
                typeof CasActuatorEndpoints[name] === "function" &&
                !["length", "name", "prototype", "all"].includes(name)
            )
            .map(name => ({ name, value: CasActuatorEndpoints[name]() }));
    }
}
