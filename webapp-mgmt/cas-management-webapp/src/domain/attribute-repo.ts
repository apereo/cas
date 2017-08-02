export abstract class PrincipalAttributesRepository {
  expiration: number;
  timeUnit: String;
  mergingStrategy: String;
}

export abstract class AbstractPrincipalAttributesRepository extends PrincipalAttributesRepository {

}

export class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository";
  }
}

export class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository";
  }
}
