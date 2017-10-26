export abstract class RegisteredServiceAccessStrategy {
  enabled = true;
  ssoEnabled = false;
  unauthorizedRedirectUrl: String;
  requireAllAttributes = false;
  requiredAttributes: Map<String, String[]>;
  rejectedAttributes: Map<String, String[]>;
  caseInsensitive: boolean;

  constructor(strat?: RegisteredServiceAccessStrategy) {
    this.enabled = (strat && strat.enabled) || true;
    this.ssoEnabled = strat && strat.ssoEnabled;
    this.unauthorizedRedirectUrl = strat && strat.unauthorizedRedirectUrl;
    this.requiredAttributes = strat && strat.requiredAttributes;
    this.requireAllAttributes = strat && strat.requireAllAttributes;
    this.rejectedAttributes = strat && strat.rejectedAttributes;
    this.caseInsensitive = strat && strat.caseInsensitive;
  }
}

export class DefaultRegisteredServiceAccessStrategy extends RegisteredServiceAccessStrategy {
  static cName = 'org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultRegisteredServiceAccessStrategy.cName;
  }

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this['@class'] = DefaultRegisteredServiceAccessStrategy.cName;
  }
}

export class RemoteEndpointServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  static cName = 'org.apereo.cas.services.RemoteEndpointServiceAccessStrategy';

  endpointUrl: String;
  acceptableResponseCodes: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === RemoteEndpointServiceAccessStrategy.cName;
  }

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this['@class'] = RemoteEndpointServiceAccessStrategy.cName;
  }
}

export class TimeBasedRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  static cName = 'org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy';

  startingDateTime: String;
  endingDateTime: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === TimeBasedRegisteredServiceAccessStrategy.cName;
  }

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this['@class'] = TimeBasedRegisteredServiceAccessStrategy.cName;
  }
}

export class GrouperRegisteredServiceAccessStrategy extends TimeBasedRegisteredServiceAccessStrategy {
  static cName = 'org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy';

  groupField: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === GrouperRegisteredServiceAccessStrategy.cName;
  }

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this['@class'] = GrouperRegisteredServiceAccessStrategy.cName
  }
}

export class SurrogateRegisteredServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
  static cName = 'org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy';

  surrogateEnabled: boolean;
  surrogateRequiredAttributes: Map<String, String[]>;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === SurrogateRegisteredServiceAccessStrategy.cName;
  }

  constructor(strat?: RegisteredServiceAccessStrategy) {
    super(strat);
    this['@class'] = SurrogateRegisteredServiceAccessStrategy.cName;
  }
}
