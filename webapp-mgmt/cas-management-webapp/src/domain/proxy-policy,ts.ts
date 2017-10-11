export abstract class RegisteredServiceProxyPolicy {

}

export class RegexMatchingRegisteredServiceProxyPolicy extends RegisteredServiceProxyPolicy {
  pattern: String;

  static cName =  "org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy";

  constructor() {
    super();
    this["@class"] = RegexMatchingRegisteredServiceProxyPolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === RegexMatchingRegisteredServiceProxyPolicy.cName;
  }
}

export class RefuseRegisteredServiceProxyPolicy extends RegisteredServiceProxyPolicy {
  static cName = "org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy";

  constructor() {
    super();
    this["@class"] = RefuseRegisteredServiceProxyPolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === RefuseRegisteredServiceProxyPolicy.cName;
  }
}
