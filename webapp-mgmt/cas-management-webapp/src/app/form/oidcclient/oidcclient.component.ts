import {Component, Input, OnInit} from '@angular/core';
import {OidcRegisteredService} from "../../../domain/oauth-service";
import {Messages} from "../../messages";
import {Data} from "../data";

@Component({
  selector: 'app-oidcclient',
  templateUrl: './oidcclient.component.html',
  styleUrls: ['./oidcclient.component.css']
})
export class OidcclientComponent implements OnInit {

  selectOptions;
  service: OidcRegisteredService;

  constructor(public messages: Messages,
              public data: Data) {
    this.selectOptions = data.selectOptions;
    this.service = data.service as OidcRegisteredService;
  }

  ngOnInit() {
    if(!this.service.scopes) {
      this.service.scopes = [];
    }
  }

}
