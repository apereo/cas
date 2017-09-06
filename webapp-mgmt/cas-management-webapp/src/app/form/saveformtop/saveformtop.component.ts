import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {Messages} from "../../messages";
import {Location} from "@angular/common";
import {Data} from "../data";

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
              public data: Data) {
  }

  ngOnInit() {
  }

  saveFn() {
    this.save.emit();
    this.data.save.emit();
    this.data.submitted = true;
  }

  goBack() {
    this.location.back();
  }

  isNew() {
    return this.data.service.id == -1;
  }

  isView() {
    return this.location.path().indexOf("view") > -1;
  }

}
