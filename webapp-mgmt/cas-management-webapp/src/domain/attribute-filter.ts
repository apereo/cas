export abstract class RegisteredServiceAttributeFilter {
    constructor() {
    }
}

export class RegisteredServiceChainingAttributeFilter extends RegisteredServiceAttributeFilter {
    filters: RegisteredServiceAttributeFilter[];

    static cName = "org.apereo.cas.services.support.RegisteredServiceChainingAttributeFilter";

    constructor() {
        super();
        this.filters = [];
        this["@class"] = RegisteredServiceChainingAttributeFilter.cName;
    }

    static instanceof(obj: any): boolean {
        return obj && obj["@class"] === RegisteredServiceChainingAttributeFilter.cName;
    }
}

export class RegisteredServiceRegexAttributeFilter extends RegisteredServiceAttributeFilter {
    order: number;
    pattern: String;
    static cName = "org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter";

    constructor(filter?) {
        super();
        this["@class"] = RegisteredServiceRegexAttributeFilter.cName;
    }

    static instanceOf(obj: any): boolean {
        return obj["@class"] === RegisteredServiceRegexAttributeFilter.cName;
    }
}

export class RegisteredServiceMappedRegexAttributeFilter extends RegisteredServiceAttributeFilter {
    patterns: Map<String,String>;
    excludeUnmappedAttributes: boolean;
    completeMatch: boolean;
    order: number;

    static cName = "org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter";

    constructor() {
        super();
        this.patterns = new Map<String,String>();
        this["@class"] = RegisteredServiceMappedRegexAttributeFilter.cName;
    }

    static instanceof(obj: any): boolean {
       return obj && obj["@class"] === RegisteredServiceMappedRegexAttributeFilter.cName;
    }
}

export class RegisteredServiceReverseMappedRegexAttributeFilter extends RegisteredServiceMappedRegexAttributeFilter {
    static cName = "org.apereo.cas.services.support.RegisteredServiceReverseMappedRegexAttributeFilter";

    constructor() {
        super();
        this["@class"] = RegisteredServiceReverseMappedRegexAttributeFilter.cName;
    }

    static instanceof(obj: any): boolean {
        return obj && obj["@class"] === RegisteredServiceReverseMappedRegexAttributeFilter.cName;
    }
}

export class RegisteredServiceScriptedAttributeFilter extends RegisteredServiceAttributeFilter {
    script: String;
    order: number;

    static cName = "org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilter";

    constructor() {
        super();
        this["@class"] = RegisteredServiceScriptedAttributeFilter.cName;
    }

    static instanceof(obj: any): boolean {
        return obj && obj["@class"] === RegisteredServiceScriptedAttributeFilter.cName;
    }
}



