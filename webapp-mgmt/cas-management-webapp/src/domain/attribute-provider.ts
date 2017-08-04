export abstract class RegisteredServiceUsernameAttributeProvider {
  canonicalizationMode: String;
  encryptUserName: String;
}

export abstract class BaseRegisteredServiceUsernameAtttributeProvider extends RegisteredServiceUsernameAttributeProvider {

}

export class DefaultRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  static cName = "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider";

  constructor() {
    super();
    this["@class"] = DefaultRegisteredServiceUsernameProvider.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === DefaultRegisteredServiceUsernameProvider.cName;
  }
}

export class PrincipalAttributeRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  usernameAttribute: String;

  static cName = "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider";

  constructor(){
    super();
    this["@class"] = PrincipalAttributeRegisteredServiceUsernameProvider.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === PrincipalAttributeRegisteredServiceUsernameProvider.cName;
  }
}

export class AnonymousRegisteredServiceUsernameProvider extends RegisteredServiceUsernameAttributeProvider {
  persistentIdGenerator: ShibbolethCompatiblePersistentIdGenerator;

  static cName = "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider";

  constructor(){
    super();
    this["@class"] = AnonymousRegisteredServiceUsernameProvider.cName;
    this.persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator();
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === AnonymousRegisteredServiceUsernameProvider.cName;
  }
}

export class ShibbolethCompatiblePersistentIdGenerator {
  salt: String;
  constructor() {
    this["@class"] = "org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator";
  }
}
