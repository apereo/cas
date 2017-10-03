import {Component, OnInit, ViewChild} from '@angular/core';
import {Data} from "../data";
import {Messages} from "../../messages";
import {DefaultRegisteredServiceContact} from "../../../domain/contact";
import {MdTabGroup} from "@angular/material";

@Component({
  selector: 'app-contacts',
  templateUrl: './contacts.component.html',
  styleUrls: ['./contacts.component.css']
})
export class ContactsComponent implements OnInit {

  selectedTab: number;

  constructor(public messages: Messages,
              public data: Data) { }

  ngOnInit() {
    if (!this.data.service.contacts) {
      this.data.service.contacts = [];
    }
  }

  addContact() {
    this.data.service.contacts.push(new DefaultRegisteredServiceContact());
    this.selectedTab = this.data.service.contacts.length - 1;
  }

  deleteContact() {
    if (this.selectedTab > -1) {
      this.data.service.contacts.splice(this.selectedTab, 1);
    }
  }

}
