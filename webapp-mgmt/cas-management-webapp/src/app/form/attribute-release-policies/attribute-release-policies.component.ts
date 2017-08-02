import {Component, OnInit, Input} from '@angular/core';
import {FormData } from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  DenyAllAttributeReleasePolicy, GroovyScriptAttributeReleasePolicy, ReturnAllAttributeReleasePolicy,
  ReturnMappedAttributeReleasePolicy, ScriptedRegisteredServiceAttributeReleasePolicy
} from "../../../domain/attribute-release";

@Component({
  selector: 'app-attribute-release-policies',
  templateUrl: './attribute-release-policies.component.html',
  styleUrls: ['./attribute-release-policies.component.css']
})
export class AttributeReleasePoliciesComponent implements OnInit {
  @Input()
  service: AbstractRegisteredService;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  type: String;

  constructor(public messages: Messages) { }

  ngOnInit() {
    switch(this.service.attributeReleasePolicy["@class"]) {
      case "org.apereo.cas.services.ReturnAllAttributeReleasePolicy" :
        this.type = "all";
        break;
      case "org.apereo.cas.services.DenyAllAttributeReleasePolicy" :
        this.type = "deny";
        break;
      case "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy" :
        this.type = "mapped";
        break;
      case "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy" :
        this.type = "allowed";
        break;
      case "org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy" :
        this.type = "script";
        break;
      case "org.apereo.cas.services.GroovyScriptAttributeReleasePolicy" :
        this.type = "groovy";
        break;
    }
  }

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

  changeType() {
    switch(this.type) {
      case "all" :
        this.service.attributeReleasePolicy = new ReturnAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case "deny" :
        this.service.attributeReleasePolicy = new DenyAllAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case "mapped" :
        let mapped: ReturnMappedAttributeReleasePolicy = new ReturnMappedAttributeReleasePolicy(this.service.attributeReleasePolicy);
        mapped.allowedAttributes = new Map();
        this.formData.availableAttributes.forEach((item: any) => {
          mapped.allowedAttributes[item] = [item];
        });
        this.service.attributeReleasePolicy = mapped;
        break;
      case "script" :
        this.service.attributeReleasePolicy = new ScriptedRegisteredServiceAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
      case "groovy" :
        this.service.attributeReleasePolicy = new GroovyScriptAttributeReleasePolicy(this.service.attributeReleasePolicy);
        break;
    }
  }

}
