/**
 * Created by tsschmi on 2/28/17.
 */
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms"
import {AlertComponent} from "../alert/alert.component";
import 'hammerjs';
import {MyTooltipDirective} from "../my-tooltip.directive";

@NgModule({
  imports: [
    CommonModule,
    FormsModule
  ],
  declarations: [
    AlertComponent,
    MyTooltipDirective,
  ],
  providers: [
  ],
  exports: [
    AlertComponent,
    MyTooltipDirective,
  ]
})
export class SharedModule {}
