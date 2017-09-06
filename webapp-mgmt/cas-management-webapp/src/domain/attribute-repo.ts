export abstract class PrincipalAttributesRepository {
  expiration: number;
  timeUnit: String;
  mergingStrategy: String;
}

export abstract class AbstractPrincipalAttributesRepository extends PrincipalAttributesRepository {

}

export class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
  static cName = "org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository";

  constructor() {
    super();
    this["@class"] = DefaultPrincipalAttributesRepository.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === DefaultPrincipalAttributesRepository.cName;
  }
}

export class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
  static cName = "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository";

  constructor() {
    super();
    this["@class"] = CachingPrincipalAttributesRepository.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === CachingPrincipalAttributesRepository.cName;
  }
}
