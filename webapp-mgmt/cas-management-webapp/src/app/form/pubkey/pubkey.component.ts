import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-pubkey',
  templateUrl: './pubkey.component.html'
})
export class PubkeyComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
