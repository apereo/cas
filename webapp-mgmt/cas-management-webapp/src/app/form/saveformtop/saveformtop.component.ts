import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {Messages} from "../../messages";
import {Location} from "@angular/common";
import {TabService} from "../tab.service";

@Component({
  selector: 'app-saveformtop',
  templateUrl: './saveformtop.component.html'
})
export class SaveformtopComponent implements OnInit {

  @Output()
  save: EventEmitter<void> = new EventEmitter<void>();

  back: Location;

  constructor(public messages: Messages,
              private location: Location,
              private tabService: TabService) {
  }

  ngOnInit() {
  }

  goBack() {
    this.location.back();
  }

  isNew() {
    return +this.tabService.serviceData.assignedId == -1;
  }

  isView() {
    return this.location.path().indexOf("view") > -1;
  }

}
