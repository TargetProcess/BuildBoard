/// <reference path='../_all.ts' />
module buildBoard {


    export class ServerRoutesService {
        public static NAME = "ServerRoutesService";

        public static $inject = [
            '$window'
        ];

        controllers:Controllers;

        constructor($window:IBuildBoardWindow) {
            this.controllers = $window.jsRoutes.controllers;
        }
        branches():string {
            return this.controllers.Application.branches().absoluteURL();
        }

        branch(id:string):string {
            return this.controllers.Application.branch(id).absoluteURL();
        }
    }
}