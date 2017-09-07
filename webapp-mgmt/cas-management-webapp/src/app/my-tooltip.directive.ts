import {Directive, ElementRef, Input, OnInit} from '@angular/core';

@Directive({
  selector: '[myTooltip]'
})

export class MyTooltipDirective implements OnInit{

  constructor(private el: ElementRef) {}

  ngOnInit() {
    let i: HTMLLIElement = this.el.nativeElement as HTMLLIElement;
    i.setAttribute("data-toggle","tooltip");
    i.setAttribute("data-placement","top");
    i.setAttribute("title",this.msg);
    i.setAttribute("class","fa fa-lg fa-question-circle form-tooltip-icon"+(this.noFloat ? " no-float" : ""));
  }

  @Input('myTooltip') msg: string;

  @Input() noFloat: boolean = true;
}
