import { Component, OnInit } from '@angular/core';
import {SamlRegisteredService} from "../../../../domain/saml-service";
import {Messages} from "../../../messages";
import {Data} from "../../data";

@Component({
  selector: 'app-nameid',
  templateUrl: './nameid.component.html',
  styleUrls: ['./nameid.component.css']
})
export class NameidComponent implements OnInit {
  selectOptions;
  service: SamlRegisteredService;

  constructor(public messages: Messages,
              public data: Data) {
      this.selectOptions = data.selectOptions;
      this.service = data.service as SamlRegisteredService;
  }

  ngOnInit() {
  }

}
