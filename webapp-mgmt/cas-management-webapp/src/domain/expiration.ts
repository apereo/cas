
export abstract class RegisteredServiceExpirationPolicy {
    expirationDate: String;
    deleteWhenExpired: boolean;
    notifyWhenDeleted: boolean;

    constructor() {}
}

export class DefaultRegisteredServiceExpirationPolicy extends RegisteredServiceExpirationPolicy {
    static cName = "org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy";

    constructor() {
      super();
      this["@class"] = DefaultRegisteredServiceExpirationPolicy.cName;
    }

    static instanceOf(obj: any): boolean {
        return obj && obj["@class"] === DefaultRegisteredServiceExpirationPolicy.cName;
    }
}