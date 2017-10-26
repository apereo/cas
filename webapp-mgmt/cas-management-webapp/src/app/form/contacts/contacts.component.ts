import {Component, Input, OnInit} from '@angular/core';
import {Data} from '../data';
import {Messages} from '../../messages';
import {DefaultRegisteredServiceContact} from '../../../domain/contact';
import {NgForm} from '@angular/forms';

@Component({
  selector: 'app-contacts',
  templateUrl: './contacts.component.html',
  styleUrls: ['./contacts.component.css']
})
export class ContactsComponent implements OnInit {

  selectedTab: number;

  @Input()
  form: NgForm;

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
    setTimeout(() => {
        this.form.resetForm();
    }, 100);
  }

  deleteContact() {
    if (this.selectedTab > -1) {
      this.data.service.contacts.splice(this.selectedTab, 1);
    }
  }

  getTabHeader(contact: DefaultRegisteredServiceContact) {
    return contact.name ? contact.name : this.data.service.contacts.indexOf(contact) + 1;
  }

}
