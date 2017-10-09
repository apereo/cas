export class RegisteredServiceContact {
  id: number;
  name: String;
  email: String;
  phone: String;
  department: String;
}

export class DefaultRegisteredServiceContact extends RegisteredServiceContact {

  static cName = "org.apereo.cas.services.DefaultRegisteredServiceContact";

  constructor() {
    super();
    this["@class"] = DefaultRegisteredServiceContact.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj && obj["@class"] === DefaultRegisteredServiceContact.cName;
  }
}
