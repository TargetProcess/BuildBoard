/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchRouteParams extends ng.IRouteParamsService {
        branchType:string;
        branchId:string;
    }

    export interface IBranchScope extends ng.IScope {
        branchName:string;
    }


    export class BranchController {
        public static $inject = [
            '$scope',
            '$http',
            '$routeParams',
            ServerRoutesService.NAME
        ];

        constructor(private $scope:IBranchScope, $http:ng.IHttpService, $routeParams:IBranchRouteParams, serverRoutesService:ServerRoutesService) {
            var branchType = $routeParams.branchType;
            var branchId = $routeParams.branchId;

            this.$scope.branchName = (branchId && branchType) ? branchType + '/' + branchId : (branchId || branchType);

            $http.get(serverRoutesService.branch(this.$scope.branchName)).success((data:Branch)=> {
                this.$scope.branch = data;
            });

        }
    }


}