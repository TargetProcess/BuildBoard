/// <reference path='../_all.ts' />
module buildBoard {


    export class BackendService {
        public static NAME = "backendService";

        public static $inject = [
            '$window',
            '$http'
        ];

        controllers:Controllers;

        constructor(private $window:IBuildBoardWindow, private $http:ng.IHttpService) {
            this.controllers = $window.jsRoutes.controllers;
        }


        branch(id:string):ng.IHttpPromise<Branch> {
            var url = this.controllers.Application.branch(id).absoluteURL();
            return this.$http.get(url);
        }

        branches():ng.IHttpPromise<Branch[]> {
            return this.$http.get(this.controllers.Application.branches().absoluteURL());
        }

        builds(branchId:string):ng.IHttpPromise<Build[]> {
            return this.$http.get(this.controllers.Jenkins.builds(branchId).absoluteURL());
        }

        buildsPerBuild():ng.IHttpPromise<{ [branch: string]: Build; }> {
            return this.$http.get(this.controllers.Jenkins.lastBuildInfos().absoluteURL());
        }


        forceBuild(buildAction:BuildAction):ng.IHttpPromise<Build> {
            return this.$http.get(this.controllers.Jenkins.forceBuild(buildAction.pullRequestId, buildAction.branchId, buildAction.fullCycle).absoluteURL());
        }


        getPullRequestStatus(pullRequest:number):ng.IHttpPromise<PRStatus> {
            return this.$http.get(this.controllers.Github.pullRequestStatus(pullRequest).absoluteURL());
        }

        changeEntityState(entityId:number, nextStateId:number):ng.IHttpPromise<EntityState> {
            return this.$http.post(this.controllers.Targetprocess.changeEntityState(entityId,nextStateId).absoluteURL(), {});
        }
    }

}