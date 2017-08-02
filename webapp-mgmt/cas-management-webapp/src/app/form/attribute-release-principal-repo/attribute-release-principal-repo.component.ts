import {Component, OnInit, Input} from '@angular/core';
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  CachingPrincipalAttributesRepository,
  DefaultPrincipalAttributesRepository
} from "../../../domain/attribute-repo";

@Component({
  selector: 'app-attribute-release-principal-repo',
  templateUrl: './attribute-release-principal-repo.component.html',
  styleUrls: ['./attribute-release-principal-repo.component.css']
})
export class AttributeReleasePrincipalRepoComponent implements OnInit {
  @Input()
  service: AbstractRegisteredService;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  type: String;

  constructor(public messages: Messages) { }

  ngOnInit() {
    switch(this.service.attributeReleasePolicy.principalAttributesRepository["@class"]) {
      case "org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository" :
        this.type = "DEFAULT";
        break;
      case "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository" :
        this.type = "CACHED";
        break;
    }
  }

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

  changeType() {
    switch(this.type) {
      case 'DEFAULT' :
        this.service.attributeReleasePolicy.principalAttributesRepository = new DefaultPrincipalAttributesRepository();
        break;
      case 'CACHED' :
        this.service.attributeReleasePolicy.principalAttributesRepository = new CachingPrincipalAttributesRepository();
        break;
    }
  }

}
