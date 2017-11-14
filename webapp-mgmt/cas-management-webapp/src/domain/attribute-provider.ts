export abstract class RegisteredServiceUsernameAttributeProvider {
  canonicalizationMode: String;
  encryptUserName: String;
}

export abstract class BaseRegisteredServiceUsernameAtttributeProvider extends RegisteredServiceUsernameAttributeProvider {

  constructor(provider?: RegisteredServiceUsernameAttributeProvider) {
    super();
    this.canonicalizationMode = (provider && provider.canonicalizationMode) || 'NONE';
    this.encryptUserName = provider && provider.encryptUserName;
  }

}

export class DefaultRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  static cName = 'org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultRegisteredServiceUsernameProvider.cName;
  }

  constructor(provider?: RegisteredServiceUsernameAttributeProvider) {
    super(provider);
    this['@class'] = DefaultRegisteredServiceUsernameProvider.cName;
  }
}

export class PrincipalAttributeRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAtttributeProvider {
  static cName = 'org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider';

  usernameAttribute: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === PrincipalAttributeRegisteredServiceUsernameProvider.cName;
  }

  constructor(provider?: RegisteredServiceUsernameAttributeProvider) {
    super(provider);
    this['@class'] = PrincipalAttributeRegisteredServiceUsernameProvider.cName;
  }
}

export class ShibbolethCompatiblePersistentIdGenerator {
  static cName = 'org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator';

  salt: String;
  attribute: String;

  static instsanceOf(obj: any): boolean {
    return obj && obj['@class'] === ShibbolethCompatiblePersistentIdGenerator.cName;
  }

  constructor() {
    this['@class'] = ShibbolethCompatiblePersistentIdGenerator.cName
  }
}

export class AnonymousRegisteredServiceUsernameProvider extends RegisteredServiceUsernameAttributeProvider {
  static cName = 'org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider';

  persistentIdGenerator: ShibbolethCompatiblePersistentIdGenerator;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === AnonymousRegisteredServiceUsernameProvider.cName;
  }

  constructor() {
    super();
    this['@class'] = AnonymousRegisteredServiceUsernameProvider.cName;
    this.persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator();
  }
}
