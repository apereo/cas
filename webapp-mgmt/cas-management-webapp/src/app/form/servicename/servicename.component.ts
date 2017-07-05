import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-servicename',
  templateUrl: './servicename.component.html'
})
export class ServicenameComponent implements OnInit {

  @Input()
  serviceData: Data;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
