export abstract class RegisteredServiceUsernameAttributeProvider {
  canonicalizationMode: String;
  encryptUserName: String;
}

export abstract class BaseRegisteredServiceUsernameAtttributeProvider extends RegisteredServiceUsernameAttributeProvider {

}

export class DefaultRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider";
  }
}

export class GroovyRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  groovyScript: String;
  constructor(){
    super();
    this["@class"] = "org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider";
  }
}

export class PrincipalAttributeRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  usernameAttribute: String;
  constructor(){
    super();
    this["@class"] = "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider";
  }
}

export class AnonymousRegisteredServiceUsernameProvider extends RegisteredServiceUsernameAttributeProvider {
  persistentIdGenerator: ShibbolethCompatiblePersistentIdGenerator;
  constructor(){
    super();
    this["@class"] = "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider";
    this.persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator();
  }
}

export class ScriptRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  script: String;
  constructor(){
    super();
    this["@class"] = "org.apereo.cas.services.ScriptRegisteredServiceUsernameProvider";
  }
}

export class ShibbolethCompatiblePersistentIdGenerator {
  salt: String;
  constructor() {
    this["@class"] = "org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator";
  }
}
