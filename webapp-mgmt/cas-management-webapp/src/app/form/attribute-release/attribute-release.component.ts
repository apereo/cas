import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data, FormData } from "../../../domain/form";

@Component({
  selector: 'app-attribute-release',
  templateUrl: './attribute-release.component.html',
})
export class AttributeReleaseComponent implements OnInit {

  @Input()
  serviceData: Data;

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
