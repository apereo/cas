import {Component, OnInit, Output, EventEmitter} from '@angular/core';
import { Router } from "@angular/router";
import {Messages} from "../messages";
import {ControlsService} from "./controls.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-controls',
  templateUrl: './controls.component.html',
  styleUrls: ['./controls.component.css']
})

export class ControlsComponent implements OnInit {

  showEdit: boolean;

  @Output()
  save: EventEmitter<void> = new EventEmitter<void>();

  constructor(public messages: Messages,
              public service: ControlsService,
              private router: Router,
              public location: Location){

  }

  ngOnInit() {
    this.showEdit = this.router.url.includes("form");
  }

  goBack() {
    this.location.back();
  }
}
