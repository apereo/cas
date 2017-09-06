export abstract class RegisteredServiceAccessStrategy {
  enabled: boolean = true;
  ssoEnabled: boolean = false;
  unauthorizedRedirectUrl: String;
  requireAllAttributes: boolean = false;
  requiredAttributes: Map<String, String[]>;
  rejectedAttributes: Map<String, String[]>;
  caseInsensitive: boolean;

  constructor(strat?: RegisteredServiceAccessStrategy) {
    this.enabled = (strat && strat.enabled) || true;
    this.ssoEnabled = strat && strat.ssoEnabled;
    this.unauthorizedRedirectUrl = strat && strat.unauthorizedRedirectUrl;
    this.requiredAttributes = strat && strat.requiredAttributes;
    this.requireAllAttributes = strat &&strat.requireAllAttributes;
    this.rejectedAttributes = strat && strat.rejectedAttributes;
    this.caseInsensitive = strat && strat.caseInsensitive;
  }
}

export class DefaultRegisteredServiceAccessStrategy extends RegisteredServiceAccessStrategy {
  static cName: string = "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy";

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = DefaultRegisteredServiceAccessStrategy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === DefaultRegisteredServiceAccessStrategy.cName;
  }

}

export class RemoteEndpointServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  endpointUrl: String;
  acceptableResponseCodes: String;

  static cName: string = "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy";

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = RemoteEndpointServiceAccessStrategy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === RemoteEndpointServiceAccessStrategy.cName;
  }
}

export class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  startingDateTime: String;
  endingDateTime: String;

  static cName: string = "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy";

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = TimeBasedRegisteredServiceAccessStrategy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === TimeBasedRegisteredServiceAccessStrategy.cName;
  }
}

export class GrouperRegisteredServiceAccessStrategy extends TimeBasedRegisteredServiceAccessStrategy {
  groupField: String;

  static cName: string = "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy";

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = GrouperRegisteredServiceAccessStrategy.cName
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === GrouperRegisteredServiceAccessStrategy.cName;
  }
}

export class SurrogateRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  surrogateEnabled: boolean;
  surrogateRequiredAttributes: Map<String, String[]>;

  static cName: string = "org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy";

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this["@class"] = SurrogateRegisteredServiceAccessStrategy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj && obj["@class"] === SurrogateRegisteredServiceAccessStrategy.cName;
  }
}
