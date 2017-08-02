import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {DefaultRegisteredServiceProperty} from "../../../domain/property";

@Component({
  selector: 'app-propertiespane',
  templateUrl: './propertiespane.component.html'
})
export class PropertiespaneComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  rows: Row[];
  addName: String;
  addValue: String[];
  isAdding: boolean;

  constructor(public messages: Messages) { }

  ngOnInit() {
    this.rows = [];
    if (!this.service.properties || Object.keys(this.service.properties).length == 0) {
      this.service.properties = new Map();
    }

    for( let entry of Array.from(Object.keys(this.service.properties))) {
      this.rows.push(new Row(entry,this.service.properties[entry].values));
    }
  }

  addRow() {
    let p: DefaultRegisteredServiceProperty = new DefaultRegisteredServiceProperty();
    p.values = this.addValue;
    let r: Row = new Row(this.addName, this.addValue);
    this.service.properties[this.addName as string] = p;
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
    this.service.properties.delete(r.tmpName);
    this.rows.splice(this.rows.indexOf(r),1);
  }

}

class Row {

  constructor(k: String, v: String[]) {
    this.property = {"name":k,"value":v};
    this.tmpName = k;
    this.tmpValue = v;

  }
  isEditing: boolean;
  property: any;
  tmpName: String;
  tmpValue: String[];
}
