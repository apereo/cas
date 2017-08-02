export abstract class RegisteredServiceAccessStrategy {
  enabled: boolean;
  ssoEnabled: boolean;
  unauthorizedRedirectUrl: String;
  requireAllAttributes: boolean;
  requiredAttributes: Map<String, String[]>;
  rejectedAttributes: Map<String, String[]>;
  caseInsensitive: boolean;

  constructor(strat?: RegisteredServiceAccessStrategy) {
    this.enabled = strat && strat.enabled;
    this.ssoEnabled = strat && strat.ssoEnabled;
    this.unauthorizedRedirectUrl = strat && strat.unauthorizedRedirectUrl;
    this.requiredAttributes = strat && strat.requiredAttributes;
    this.requireAllAttributes = strat &&strat.requireAllAttributes;
    this.rejectedAttributes = strat && strat.rejectedAttributes;
    this.caseInsensitive = strat && strat.caseInsensitive;
  }
}

export class DefaultRegisteredServiceAccessStrategy extends RegisteredServiceAccessStrategy {
  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy";
  }
}

export class RemoteEndpointServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  endpointUrl: String;
  acceptableResponseCodes: String;

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy";
  }
}

export class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  startingDateTime: String;
  endingDateTime: String;

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy";
  }
}

export class GrouperRegisteredServiceAccessStrategy extends TimeBasedRegisteredServiceAccessStrategy {
  groupField: String;

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy";
  }
}
