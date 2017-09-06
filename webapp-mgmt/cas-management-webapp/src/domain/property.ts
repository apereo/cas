export abstract class RegisteredServiceProperty {
  id: number;
  values: String[];
  constructor() {
    this.values = [];
  }
}

export class DefaultRegisteredServiceProperty extends RegisteredServiceProperty {
  constructor(){
    super();
    this["@class"] = "org.apereo.cas.services.DefaultRegisteredServiceProperty";
  }
}
