import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-samlclient',
  templateUrl: './samlclient.component.html'
})
export class SamlclientComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService

  @Input()
  selectOptions;

  type: String;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
