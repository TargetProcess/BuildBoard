/// <reference path='../_all.ts' />

module buildBoard {
    export interface IFilter {
        getCount(branches:Branch[]):number
    }

    export class Filter implements IFilter {
        constructor(public predicate:(branch:Branch)=>boolean){}

        getCount(branches:Branch[]):number {
            return _.filter(branches, this.predicate).length;
        }
    }}