/// <reference path='../_all.ts' />
module buildBoard {

    export function activeFilter() {
        return function (isActive:boolean) {
            return isActive ? 'active' : '';
        };
    }

    export function encode() {
        return function (text:string) {
            return encodeURIComponent(text);
        };
    }
}
