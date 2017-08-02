import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {FormData} from "../../../domain/service-view-bean";
import {RegisteredServiceRegexAttributeFilter} from "../../../domain/attribute-release";

@Component({
  selector: 'app-attribute-release',
  templateUrl: './attribute-release.component.html',
})
export class AttributeReleaseComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  type: String;


  constructor(public messages: Messages) { }

  ngOnInit() {

  }

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

}
