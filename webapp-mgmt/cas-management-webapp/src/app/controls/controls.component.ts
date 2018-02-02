import {Component, OnInit, Output, EventEmitter, Input} from '@angular/core';
import { Router } from '@angular/router';
import {Messages} from '../messages';
import {ControlsService} from './controls.service';
import {Location} from '@angular/common';

@Component({
  selector: 'app-controls',
  templateUrl: './controls.component.html',
  styleUrls: ['./controls.component.css']
})

export class ControlsComponent implements OnInit {

  @Input()
  showEdit: boolean;

  @Output()
  save: EventEmitter<void> = new EventEmitter<void>();

  constructor(public messages: Messages,
              public service: ControlsService,
              public location: Location) {

  }

  ngOnInit() {
  }

  goBack() {
    this.location.back();
  }
}
