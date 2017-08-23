import {Component, OnInit, Input, ViewChild} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  failureModes = ["NONE","OPEN","CLOSED","PHANTOM"];
  providers = [
    {key: "mfa-duo", value: "Duo Security"},
    {key: "mfa-authy", value: "Authy Authenticator"},
    {key: "mfa-yubikey", value: "YubiKey"},
    {key: "mfa-radius", value: "RSA/RADIUS"},
    {key: "mfa-wikid", value: "WiKID"},
    {key: "mfa-gauth", value: "Google Authenitcator"},
    {key: "mfa-azure", value: "Microsoft Azure"},
    {key: "mfa-u2f", value: "FIDO U2F"},
    {key: "mfa-swivel", value: "Swivel Secure"}
  ];

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
  }

}
