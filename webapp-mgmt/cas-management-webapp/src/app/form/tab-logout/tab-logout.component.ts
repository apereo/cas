import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-logout',
  templateUrl: './tab-logout.component.html'
})
export class TabLogoutComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
