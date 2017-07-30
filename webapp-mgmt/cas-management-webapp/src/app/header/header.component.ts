import { Component, OnInit } from '@angular/core';
import {Messages} from "../messages";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit {

  isAdmin: boolean = false;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
