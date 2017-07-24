import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-logo',
  templateUrl: './logo.component.html'
})
export class LogoComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
