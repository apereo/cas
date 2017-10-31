import {ApplicationRef, Component, ViewChild, OnInit} from '@angular/core';
import 'rxjs/add/operator/switchMap';
import {TabBaseComponent} from '../tab-base';
import {Messages} from '../../messages';
import {Data} from '../data';


@Component({
    selector: 'app-service-wizard',
    templateUrl: './service-wizard.component.html',
    styleUrls: ['./service-wizard.component.css']
})
export class ServiceWizardComponent implements OnInit {
    serviceTypeList = [
        {name: 'CAS Client', value: 'cas'},
        {name: 'OAuth2 Client', value: 'oauth'},
        {name: 'SAML2 Service Provider', value: 'saml'},
        {name: 'OpenID Connect Client', value: 'oidc'},
        {name: 'WS Federation', value: 'wsfed'}
    ];

    scopes = ['Profile', 'Email', 'Address', 'Phone', 'User Defined'];
    returnAllowedList = ['eppn', 'givenName', 'uid'];

    service: object;

    constructor(public messages: Messages,
                public data: Data) {
    }

    ngOnInit() {
        console.log(this.data.formData.serviceTypes);
    }

    handleReviewDefaults(formData) {
        console.log('handleReviewDefaults - not wired up');
    }

    handleStepThroughSettings(formData: any) {
        console.log('handleStepThroughSettings - not wired up');
    }

}
