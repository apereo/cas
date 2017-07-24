import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-reqhandlers',
  templateUrl: './reqhandlers.component.html'
})
export class ReqhandlersComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
