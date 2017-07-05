import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  @Input()
  serviceData: Data;

  @Input()
  selectOptions;

  @Input()
  isAdmin: boolean;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
