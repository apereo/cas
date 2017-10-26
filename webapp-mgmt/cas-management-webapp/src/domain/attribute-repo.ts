export abstract class PrincipalAttributesRepository {
  expiration: number;
  timeUnit: String;
  mergingStrategy: String;
}

export abstract class AbstractPrincipalAttributesRepository extends PrincipalAttributesRepository {

}

export class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
  static cName = 'org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultPrincipalAttributesRepository.cName;
  }

  constructor() {
    super();
    this['@class'] = DefaultPrincipalAttributesRepository.cName;
  }
}

export class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
  static cName = 'org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === CachingPrincipalAttributesRepository.cName;
  }

  constructor() {
    super();
    this['@class'] = CachingPrincipalAttributesRepository.cName;
  }
}
