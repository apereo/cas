import {Component, OnInit, Input} from '@angular/core';
import {FormData, ServiceData} from "../../../domain/service-edit-bean";
import {Messages} from "../../messages";

@Component({
  selector: 'app-attribute-release-filters',
  templateUrl: './attribute-release-filters.component.html',
  styleUrls: ['./attribute-release-filters.component.css']
})
export class AttributeReleaseFiltersComponent implements OnInit {
  @Input()
  serviceData: ServiceData;

  @Input()
  formData: FormData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

  isEmpty(data: any[]) {
    return data != null && data.length == 0;
  }

}
