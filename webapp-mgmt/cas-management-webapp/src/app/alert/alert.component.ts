import { Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.css']
})
export class AlertComponent implements OnInit {

  msg: String;
  type: String;
  showing: boolean;

  constructor() { }

  ngOnInit() {
  }

  setMsg(msg: String) {
    this.msg = msg;
  }

  setType(type: String) {
    this.type = type;
  }

  show(msg: String, type: String) {
    this.msg = msg;
    this.type = type;
    this.showing = true;
  }

  hide() {
    this.showing = false;
  }

}
