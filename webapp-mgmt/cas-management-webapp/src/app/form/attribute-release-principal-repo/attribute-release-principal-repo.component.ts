import {Component, OnInit } from '@angular/core';
import {FormData} from '../../../domain/form-data';
import {Messages} from '../../messages';
import {
  CachingPrincipalAttributesRepository,
  DefaultPrincipalAttributesRepository
} from '../../../domain/attribute-repo';
import {Data} from '../data';

enum Type {
  DEFAULT,
  CACHING,
}

@Component({
  selector: 'app-attribute-release-principal-repo',
  templateUrl: './attribute-release-principal-repo.component.html',
  styleUrls: ['./attribute-release-principal-repo.component.css']
})
export class AttributeReleasePrincipalRepoComponent implements OnInit {
  formData: FormData;
  type: Type;
  TYPE = Type;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
    if (DefaultPrincipalAttributesRepository.instanceOf(this.data.service.attributeReleasePolicy.principalAttributesRepository)) {
      this.type = Type.DEFAULT;
    } else if (CachingPrincipalAttributesRepository.instanceOf(this.data.service.attributeReleasePolicy.principalAttributesRepository)) {
      this.type = Type.CACHING;
    }
  }

  changeType() {
    switch (+this.type) {
      case Type.DEFAULT :
        this.data.service.attributeReleasePolicy.principalAttributesRepository = new DefaultPrincipalAttributesRepository();
        break;
      case Type.CACHING :
        this.data.service.attributeReleasePolicy.principalAttributesRepository = new CachingPrincipalAttributesRepository();
        break;
    }
  }

}
