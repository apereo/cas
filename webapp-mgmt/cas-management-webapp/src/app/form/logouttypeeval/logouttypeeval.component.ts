import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-logouttypeeval',
  templateUrl: './logouttypeeval.component.html'
})
export class LogouttypeevalComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
