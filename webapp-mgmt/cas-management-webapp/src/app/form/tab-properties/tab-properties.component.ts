import { Component, OnInit } from '@angular/core';
import {TabService} from "../tab.service";

@Component({
  selector: 'app-tab-properties',
  templateUrl: './tab-properties.component.html'
})
export class TabPropertiesComponent implements OnInit {

  constructor(public tabService: TabService) { }

  ngOnInit() {
  }

}
