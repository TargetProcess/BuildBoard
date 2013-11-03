/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export function activeFilter(){
         return function(isActive:boolean){
            return isActive ? 'active' : '';
         };
    }
}
