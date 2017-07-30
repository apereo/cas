import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData,FormData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-access-strategy',
  templateUrl: './access-strategy.component.html',
})
export class AccessStrategyComponent implements OnInit {

  @Input()
  formData: FormData;

  @Input()
  serviceData: ServiceData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
