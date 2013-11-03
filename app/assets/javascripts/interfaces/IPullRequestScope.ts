/// <reference path='../_all.ts' />

module buildBoard {
    export interface IPullRequestScope extends ng.IScope {
        getClass(status:PRStatus):string;
        branch:Branch;
        pullRequestStatus:PRStatus;
    }
}