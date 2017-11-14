import {Component, OnInit} from '@angular/core';
import {FormData} from '../../../domain/form-data';
import {Messages} from '../../messages';
import {Data} from '../data';
import {WsFederationClaimsReleasePolicy} from '../../../domain/attribute-release';
import {Util} from '../../util/util';
import {Row, RowDataSource} from '../row';


@Component({
  selector: 'app-wsfedattrrelpolicies',
  templateUrl: './wsfedattrrelpolicies.component.html',
  styleUrls: ['./wsfedattrrelpolicies.component.css']
})
export class WsfedattrrelpoliciesComponent implements OnInit {

  formData: FormData;
  wsFedOnly: boolean;

  displayedColumns = ['source', 'mapped'];
  dataSource: RowDataSource;


  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
    const attrPolicy: WsFederationClaimsReleasePolicy = this.data.service.attributeReleasePolicy as WsFederationClaimsReleasePolicy;
    const rows = [];
    if (Util.isEmpty(attrPolicy.allowedAttributes)) {
      attrPolicy.allowedAttributes = new Map();
    }

    this.formData.availableAttributes.forEach((k) => {
      attrPolicy.allowedAttributes[k as string] = k;
    });

    for (const key of Array.from(Object.keys(attrPolicy.allowedAttributes))) {
      rows.push(new Row(key as string));
    }
    this.dataSource = new RowDataSource(rows);
  }

  isEmpty(data: any[]): boolean {
    return !data || data.length === 0;
  }

}
