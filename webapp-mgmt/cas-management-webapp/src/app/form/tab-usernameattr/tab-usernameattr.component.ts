import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-usernameattr',
  templateUrl: './tab-usernameattr.component.html'
})
export class TabUsernameattrComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
