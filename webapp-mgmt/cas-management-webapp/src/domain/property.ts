export abstract class RegisteredServiceProperty {
  id: number;
  values: String[];

  constructor() {
    this.values = [];
  }
}

export class DefaultRegisteredServiceProperty extends RegisteredServiceProperty {
  static cName = 'org.apereo.cas.services.DefaultRegisteredServiceProperty';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultRegisteredServiceProperty.cName;
  }

  constructor() {
    super();
    this['@class'] = DefaultRegisteredServiceProperty.cName;
  }
}
