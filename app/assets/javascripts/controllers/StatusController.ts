/// <reference path="../_all.ts" />
module buildBoard {
    'use strict';

    export interface IStatusScope extends IBranchScope {
        status: String;
    }

    export class StatusController {
        public static NAME = 'statusController';

        public static $inject = [
            '$scope',
            BackendService.NAME,
            HttpServiceNotificationDecorator.NAME
        ];

        constructor(public $scope:IStatusScope, backendService:BackendService, private $http:HttpServiceNotificationDecorator) {
            var counter = 0;
            $http.addStatusChangedHandler(status => {
                this.$scope.status = status;
            });
        }
    }
}