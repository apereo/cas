import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

@Component({
  selector: 'app-pubkey',
  templateUrl: './pubkey.component.html'
})
export class PubkeyComponent implements OnInit {

  @Input()
  serviceData: Data;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
