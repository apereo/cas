import {Component, OnInit, Input} from '@angular/core';
import {FormData } from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  DenyAllAttributeReleasePolicy, GroovyScriptAttributeReleasePolicy, ReturnAllAttributeReleasePolicy,
  ReturnAllowedAttributeReleasePolicy,
  ReturnMappedAttributeReleasePolicy, ScriptedRegisteredServiceAttributeReleasePolicy
} from "../../../domain/attribute-release";
import {Data} from "../data";
import {SamlRegisteredService} from "../../../domain/saml-service";

enum Type {
  RETURN_ALL,
  DENY_ALL,
  RETURN_MAPPED,
  RETURN_ALLOWED,
  SCRIPT,
  GROOVY,
  INCOMMON,
  MATCHING
}

@Component({
  selector: 'app-attribute-release-policies',
  templateUrl: './attribute-release-policies.component.html',
  styleUrls: ['./attribute-release-policies.component.css']
})
export class AttributeReleasePoliciesComponent implements OnInit {
  service: AbstractRegisteredService;
  formData: FormData;
  selectOptions;
  type: Type;
  TYPE = Type;
  types = [Type.SCRIPT,Type.GROOVY,Type.RETURN_ALL,Type.DENY_ALL,Type.RETURN_ALLOWED,Type.RETURN_MAPPED];
  display = ["Script Engine", "Groovy Script", "Return All", "Deny All","Return Allowed","Return Mapped"];
  isSaml: boolean;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.formData = data.formData;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (ReturnAllAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = Type.RETURN_ALL;
    } else if (DenyAllAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = Type.DENY_ALL;
    } else if (ReturnMappedAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      let mapped: ReturnMappedAttributeReleasePolicy = this.service.attributeReleasePolicy as ReturnMappedAttributeReleasePolicy;
      this.formData.availableAttributes.forEach((item: any) => {
        mapped.allowedAttributes[item] = mapped.allowedAttributes[item] || [item];
      });
      this.type = Type.RETURN_MAPPED;
    } else if (ReturnAllowedAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = Type.RETURN_ALLOWED;
    } else if (ScriptedRegisteredServiceAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = Type.SCRIPT;
    } else if (GroovyScriptAttributeReleasePolicy.instanceOf(this.service.attributeReleasePolicy)) {
      this.type = Type.GROOVY;
    }
    this.isSaml = SamlRegisteredService.instanceOf(this.service);
  }

  changeType() {
    console.log("Changed Type : "+this.type)
    switch(+this.type) {
      case Type.RETURN_ALL:
        console.log("Changed to return all");
        this.service.attributeReleasePolicy = new ReturnAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case Type.DENY_ALL :
        this.service.attributeReleasePolicy = new DenyAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case Type.RETURN_MAPPED :
        let mapped: ReturnMappedAttributeReleasePolicy = this.service.attributeReleasePolicy as ReturnMappedAttributeReleasePolicy;
        mapped.allowedAttributes = new Map();
        this.formData.availableAttributes.forEach((item: any) => {
          mapped.allowedAttributes[item] = [item];
        });
        this.service.attributeReleasePolicy = mapped;
        break;
      case Type.RETURN_ALLOWED :
        this.service.attributeReleasePolicy = new ReturnAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case Type.SCRIPT :
        this.service.attributeReleasePolicy = new ScriptedRegisteredServiceAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case Type.GROOVY :
        this.service.attributeReleasePolicy = new GroovyScriptAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
    }
  }

  isEmpty(data: any[]): boolean {
    return !data || data.length == 0;
  }

}
