/// <reference path="../_all.ts" />
module buildBoard {
    export class EntityStateDirective implements ng.IDirective {
        static NAME = "entityState";

        scope = {
            entity: "="
        };

        controller = EntityStateDirectiveController;

        template = [
            '<div class="btn-group btn-group-stretch" ng-show="entity">',
            '<button type="button" class="btn dropdown-toggle" data-toggle="dropdown">',
            '{{entity.state.name}} ',
            '<span class="caret"></span>',
            '</button>',
            '<ul class="dropdown-menu">',
            '<li ng-repeat="entityState in entity.state.nextStates">',
            '<a ng-click="changeEntityState(entity, entityState.id)">{{entityState.name}}</a>',
            '</li>',
            '</ul>',
            '</div>'
        ].join("");

        restrict = "E";
        replace = true;
    }

    export interface IEntityStateDirectiveScope extends ng.IScope {
        changeEntityState(entity:Entity, nextState:number)
    }

    export class EntityStateDirectiveController {
        public $inject = ['$scope', BackendService.NAME];

        constructor($scope:IEntityStateDirectiveScope, backendService:BackendService) {
            $scope.changeEntityState = (entity:Entity, nextState:number)=> {
                backendService.changeEntityState(entity.id, nextState).success(state=> {
                    entity.state = state;
                });

            }
        }
    }
}