class CasActuatorEndpoints {
    static endpoints() {
        return PalantirDashboardConfiguration.actuatorEndpoints();
    }

    static health() {
        return this.endpoints().health;
    }

    static discoveryProfile() {
        return this.endpoints().discoveryprofile || this.endpoints().discoveryProfile;
    }

    static casConfig() {
        return this.endpoints().casconfig || this.endpoints().casConfig;
    }

    static env() {
        return this.endpoints().env;
    }

    static info() {
        return this.endpoints().info;
    }

    static throttles() {
        return this.endpoints().throttles;
    }

    static statistics() {
        return this.endpoints().statistics;
    }

    static metrics() {
        return this.endpoints().metrics;
    }

    static prometheus() {
        return this.endpoints().prometheus;
    }

    static auditEvents() {
        return this.endpoints().auditevents || this.endpoints().auditEvents;
    }

    static personDirectory() {
        return this.endpoints().persondirectory || this.endpoints().personDirectory;
    }

    static configProps() {
        return this.endpoints().configprops || this.endpoints().configProps;
    }

    static beans() {
        return this.endpoints().beans || this.endpoints().springBeans;
    }

    static conditions() {
        return this.endpoints().conditions || this.endpoints().springConditions;
    }

    static mfaDevices() {
        return this.endpoints().mfadevices || this.endpoints().mfaDevices;
    }

    static refresh() {
        return this.endpoints().busrefresh || this.endpoints().refresh;
    }

    static shutdown() {
        return this.endpoints().shutdown;
    }

    static restart() {
        return this.endpoints().restart;
    }

    static configurationMetadata() {
        return this.endpoints().configurationmetadata || this.endpoints().configurationMetadata;
    }

    static attributeConsent() {
        return this.endpoints().attributeconsent || this.endpoints().attributeConsent;
    }

    static httpExchanges() {
        return this.endpoints().httpexchanges || this.endpoints().httpExchanges;
    }

    static mappings() {
        return this.endpoints().mappings;
    }

    static serviceAccess() {
        return this.endpoints().serviceaccess || this.endpoints().serviceAccess;
    }

    static springWebflow() {
        return this.endpoints().springwebflow || this.endpoints().springWebflow;
    }

    static events() {
        return this.endpoints().events;
    }

    static auditLog() {
        return this.endpoints().auditlog || this.endpoints().auditLog;
    }

    static casFeatures() {
        return this.endpoints().casfeatures || this.endpoints().casFeatures;
    }

    static loggers() {
        return this.endpoints().loggers;
    }

    static loggingConfig() {
        return this.endpoints().loggingconfig || this.endpoints().loggingConfig;
    }

    static ticketRegistry() {
        return this.endpoints().ticketregistry || this.endpoints().ticketRegistry;
    }

    static heapDump() {
        return this.endpoints().heapdump || this.endpoints().heapDump;
    }

    static heapDumpAnalysis() {
        return this.endpoints().heapdumpanalysis || this.endpoints().heapDumpAnalysis;
    }

    static sessions() {
        return this.endpoints().sessions;
    }

    static ssoSessions() {
        return this.endpoints().ssosessions || this.endpoints().ssoSessions;
    }

    static registeredServices() {
        return this.endpoints().registeredservices || this.endpoints().registeredServices;
    }

    static oidcJwks() {
        return this.endpoints().oidcjwks || this.endpoints().oidcJwks;
    }

    static heimdall() {
        return this.endpoints().heimdall;
    }

    static logFile() {
        return this.endpoints().logfile || this.endpoints().logFile;
    }

    static scheduledTasks() {
        return this.endpoints().scheduledtasks || this.endpoints().scheduledTasks;
    }

    static multifactorTrustedDevices() {
        return this.endpoints().multifactoreddevices || this.endpoints().multifactorTrustedDevices;
    }

    static multitenancy() {
        return this.endpoints().multitenancy;
    }

    static samlPostProfileResponse() {
        return this.endpoints().samlpostprofileresponse || this.endpoints().samlPostProfileResponse;
    }

    static casValidate() {
        return this.endpoints().casvalidate || this.endpoints().casValidate;
    }

    static samlValidate() {
        return this.endpoints().samlvalidate || this.endpoints().samlValidate;
    }

    static gcpLogs() {
        return this.endpoints().gcplogs || this.endpoints().gcpLogs;
    }

    static cloudWatchLogs() {
        return this.endpoints().cloudwatchlogs || this.endpoints().cloudWatchLogs;
    }

    static attributeDefinitions() {
        return this.endpoints().attributedefinitions || this.endpoints().attributeDefinitions;
    }

    static delegatedClients() {
        return this.endpoints().delegatedclients || this.endpoints().delegatedClients;
    }

    static authenticationHandlers() {
        return this.endpoints().authenticationhandlers || this.endpoints().authenticationHandlers;
    }

    static ticketExpirationPolicies() {
        return this.endpoints().ticketexpirationpolicies || this.endpoints().ticketExpirationPolicies;
    }

    static authenticationPolicies() {
        return this.endpoints().authenticationpolicies || this.endpoints().authenticationPolicies;
    }

    static entityHistory() {
        return this.endpoints().entityhistory || this.endpoints().entityHistory;
    }

    static samlIdpRegisteredServiceMetadata() {
        return this.endpoints().samlidpregisteredservicemetadata || this.endpoints().samlIdpRegisteredServiceMetadata;
    }

    static samlIdpRegisteredServiceMetadataCache() {
        return this.endpoints().samlidpregisteredservicemetadatacache || this.endpoints().samlIdpRegisteredServiceMetadataCache;
    }

    static threadDump() {
        return this.endpoints().threaddump || this.endpoints().threadDump;
    }

    static passwordManagement() {
        return this.endpoints().passwordmanagement || this.endpoints().passwordManagement;
    }

    static impersonation() {
        return this.endpoints().impersonation;
    }

    static startup() {
        return this.endpoints().startup;
    }

    static dependencies() {
        return this.endpoints().dependencies;
    }

    static all() {
        return Object.getOwnPropertyNames(CasActuatorEndpoints)
            .filter(name =>
                typeof CasActuatorEndpoints[name] === "function" &&
                !["length", "name", "prototype", "all", "endpoints"].includes(name)
            )
            .map(name => ({ name, value: CasActuatorEndpoints[name]() }));
    }
}
