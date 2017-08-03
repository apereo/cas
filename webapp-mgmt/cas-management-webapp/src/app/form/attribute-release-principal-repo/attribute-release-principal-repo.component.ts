import {Component, OnInit, Input} from '@angular/core';
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  CachingPrincipalAttributesRepository,
  DefaultPrincipalAttributesRepository
} from "../../../domain/attribute-repo";
import {Data} from "../data";

@Component({
  selector: 'app-attribute-release-principal-repo',
  templateUrl: './attribute-release-principal-repo.component.html',
  styleUrls: ['./attribute-release-principal-repo.component.css']
})
export class AttributeReleasePrincipalRepoComponent implements OnInit {
  service: AbstractRegisteredService;
  formData: FormData;
  selectOptions;
  type: String;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.formData = data.formData;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (DefaultPrincipalAttributesRepository.instanceOf(this.service.attributeReleasePolicy.principalAttributesRepository)) {
      this.type = "DEFAULT";
    } else if (CachingPrincipalAttributesRepository.instanceOf(this.service.attributeReleasePolicy.principalAttributesRepository)) {
      this.type = "CACHED";
    }
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
