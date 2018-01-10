import {Component, OnInit} from '@angular/core';
import {FormData } from '../../../domain/form-data';
import {Messages} from '../../messages';
import {
  DenyAllAttributeReleasePolicy,
  GroovySamlRegisteredServiceAttributeReleasePolicy,
  GroovyScriptAttributeReleasePolicy,
  InCommonRSAttributeReleasePolicy,
  MetadataEntityAttributesAttributeReleasePolicy,
  PatternMatchingEntityIdAttributeReleasePolicy,
  ReturnAllAttributeReleasePolicy,
  ReturnAllowedAttributeReleasePolicy,
  ReturnMappedAttributeReleasePolicy,
  ReturnRestfulAttributeReleasePolicy,
  ScriptedRegisteredServiceAttributeReleasePolicy
} from '../../../domain/attribute-release';
import {Data} from '../data';
import {SamlRegisteredService} from '../../../domain/saml-service';
import {Util} from '../../util/util';

enum Type {
  RETURN_ALL,
  DENY_ALL,
  RETURN_MAPPED,
  RETURN_ALLOWED,
  SCRIPT,
  GROOVY,
  INCOMMON,
  MATCHING,
  METADATA,
  RESTFUL,
  GROOVY_SAML
}

@Component({
  selector: 'app-attribute-release-policies',
  templateUrl: './attribute-release-policies.component.html',
  styleUrls: ['./attribute-release-policies.component.css']
})
export class AttributeReleasePoliciesComponent implements OnInit {
  formData: FormData;
  type: Type;
  TYPE = Type;
  types = [Type.SCRIPT, Type.GROOVY, Type.RETURN_ALL, Type.DENY_ALL, Type.RETURN_ALLOWED, Type.RETURN_MAPPED, Type.RESTFUL];
  display = ['Script Engine', 'Groovy Script', 'Return All', 'Deny All', 'Return Allowed', 'Return Mapped', 'Return Restful'];
  isSaml: boolean;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
    if (ReturnAllAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.RETURN_ALL;
    } else if (DenyAllAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.DENY_ALL;
    } else if (ReturnMappedAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      const mapped: ReturnMappedAttributeReleasePolicy = this.data.service.attributeReleasePolicy as ReturnMappedAttributeReleasePolicy;
      if (Util.isEmpty(mapped.allowedAttributes)) {
        mapped.allowedAttributes = new Map();
      }
      this.type = Type.RETURN_MAPPED;
    } else if (ReturnAllowedAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.RETURN_ALLOWED;
    } else if (ScriptedRegisteredServiceAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.SCRIPT;
    } else if (GroovyScriptAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.GROOVY;
    } else if (InCommonRSAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.INCOMMON;
    } else if (PatternMatchingEntityIdAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.MATCHING;
    } else if (MetadataEntityAttributesAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.METADATA;
    } else if (ReturnRestfulAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.RESTFUL;
    } else if (GroovySamlRegisteredServiceAttributeReleasePolicy.instanceOf(this.data.service.attributeReleasePolicy)) {
      this.type = Type.GROOVY_SAML;
    }

    this.isSaml = SamlRegisteredService.instanceOf(this.data.service);

    if (this.isSaml && this.types.indexOf(Type.INCOMMON) < 0) {
      this.types.push(Type.INCOMMON);
      this.display.push('InCommon');
      this.types.push(Type.MATCHING);
      this.display.push('Matching');
      this.types.push(Type.METADATA);
      this.display.push('Metadata Entity Attributes');
      this.types.push((Type.GROOVY_SAML));
      this.display.push('Groovy SAML Script');
    } else if (!this.isSaml && this.types.indexOf(Type.INCOMMON) > -1) {
      this.types.splice(this.types.indexOf(Type.INCOMMON), 1);
      this.display.splice(this.display.indexOf('InCommon'), 1);
      this.types.splice(this.types.indexOf(Type.MATCHING), 1);
      this.display.splice(this.display.indexOf('Matching'), 1);
      this.types.splice(this.types.indexOf(Type.METADATA), 1);
      this.display.splice(this.display.indexOf('Metadata Entity Attributes'), 1);
      this.types.splice(this.types.indexOf(Type.GROOVY_SAML), 1);
      this.display.splice(this.display.indexOf('Groovy SAML Script'), 1);
    }
  }

  changeType() {
    switch (+this.type) {
      case Type.RETURN_ALL:
        this.data.service.attributeReleasePolicy = new ReturnAllAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        break;
      case Type.DENY_ALL :
        this.data.service.attributeReleasePolicy = new DenyAllAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        break;
      case Type.RETURN_MAPPED :
        const mapped: ReturnMappedAttributeReleasePolicy = new ReturnMappedAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        mapped.allowedAttributes = new Map();
        this.data.service.attributeReleasePolicy = mapped;
        break;
      case Type.RETURN_ALLOWED :
        this.data.service.attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        break;
      case Type.SCRIPT :
        this.data.service.attributeReleasePolicy = new ScriptedRegisteredServiceAttributeReleasePolicy(
          this.data.service.attributeReleasePolicy);
        break;
      case Type.GROOVY :
        this.data.service.attributeReleasePolicy = new GroovyScriptAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        break;
      case Type.INCOMMON :
        this.data.service.attributeReleasePolicy = new InCommonRSAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        break;
      case Type.MATCHING :
        this.data.service.attributeReleasePolicy = new PatternMatchingEntityIdAttributeReleasePolicy(
          this.data.service.attributeReleasePolicy);
        break;
      case Type.METADATA :
        this.data.service.attributeReleasePolicy = new MetadataEntityAttributesAttributeReleasePolicy(
          this.data.service.attributeReleasePolicy);
        break;
      case Type.RESTFUL :
        this.data.service.attributeReleasePolicy = new ReturnRestfulAttributeReleasePolicy(this.data.service.attributeReleasePolicy);
        break;
      case Type.GROOVY_SAML :
        this.data.service.attributeReleasePolicy = new GroovySamlRegisteredServiceAttributeReleasePolicy(
          this.data.service.attributeReleasePolicy);
        break;
    }
  }

  isEmpty(data: any[]): boolean {
    return !data || data.length === 0;
  }

}
