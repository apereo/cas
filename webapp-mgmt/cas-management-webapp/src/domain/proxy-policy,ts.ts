export abstract class RegisteredServiceProxyPolicy {

}

export class RegexMatchingRegisteredServiceProxyPolicy extends RegisteredServiceProxyPolicy {
  pattern: String;
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy";
  }
}

export class RefuseRegisteredServiceProxyPolicy extends RegisteredServiceProxyPolicy {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy";
  }
}
