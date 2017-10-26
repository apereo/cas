import {ApplicationRef, Component, ViewChild} from '@angular/core';
import 'rxjs/add/operator/switchMap';
import {TabBaseComponent} from '../tab-base';
import {Subscription} from 'rxjs/Subscription';

@Component({
  selector: 'app-tab-basics',
  templateUrl: './tab-basics.component.html'
})
export class TabBasicsComponent extends TabBaseComponent  {

}
