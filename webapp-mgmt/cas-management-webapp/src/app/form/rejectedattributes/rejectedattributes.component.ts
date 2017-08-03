import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-rejectedattributes',
  templateUrl: './rejectedattributes.component.html'
})
export class RejectedattributesComponent implements OnInit {

  service: AbstractRegisteredService;

  rows: Row[];
  addName: String;
  addValue: String[];
  isAdding: boolean;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {

    this.rows = [];
    if (!this.service.accessStrategy.rejectedAttributes || Object.keys(this.service.accessStrategy.rejectedAttributes).length == 0) {
      this.service.accessStrategy.rejectedAttributes = new Map();
    }
    for (let p of Array.from(Object.keys(this.service.accessStrategy.rejectedAttributes))) {
      this.rows.push(new Row(p,this.service.accessStrategy.rejectedAttributes[p]));
    }
  }

  addRow() {
    let r: Row = new Row(this.addName, this.addValue);
    this.service.accessStrategy.rejectedAttributes[this.addName as string] = this.addValue;
    this.rows.push(r);
    this.isAdding = false;
    this.addName = null;
    this.addValue = null;
  }

  cancelAdd() {
    this.isAdding = false;
    this.addValue = null;
    this.addName = null;
  }

  save(r: Row) {
    r.property.name = r.tmpName;
    r.property.value = r.tmpValue;
    r.isEditing = false;
  }

  cancel(r: Row) {
    r.tmpName = r.property.name;
    r.tmpValue = r.property.value;
    r.isEditing = false;
  }

  delete(r: Row) {
    //this.serviceData.properties.splice(this.serviceData.properties.indexOf(r.property),1);
    this.rows.splice(this.rows.indexOf(r),1);
  }

}

class Row {

  constructor(k: String, v: String[]) {
    this.tmpName = k;
    this.tmpValue = v;
    this.property = {"name":k,"value":v};
  }
  isEditing: boolean;
  property: any;
  tmpName: String;
  tmpValue: String[];
}
