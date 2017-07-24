import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  @Input()
  selectOptions;

  @Input()
  isAdmin: boolean;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
