/// <reference path="../_all.ts" />
module buildBoard {
    export class EntityStateDirective implements ng.IDirective {
        static NAME = "entityState";

        scope = {
            entity: "=",
            type: "@"
        };

        controller = EntityStateDirectiveController;

        template = [
            '<div class="dropdown">',
            '<a href="" class="status {{type}} {{getStatusStatus(entity.state)}} dropdown-toggle" data-toggle="dropdown">{{entity.state.name}}</a>',
            '<ul class="dropdown-menu">',
            '<li ng-repeat="entityState in entity.state.nextStates"><a ng-click="changeEntityState(entityState.id)" class="status {{getStatusStatus(entityState)}}">{{entityState.name}}</a></li>',
            '</ul>',
            '</div>'
        ].join("");

        restrict = "E";
        replace = true;
    }

    export interface IEntityStateDirectiveScope extends ng.IScope {
        entity: Entity
        changeEntityState(nextState:number)
        getStatusStatus(state:EntityState):string
    }

    export class EntityStateDirectiveController {
        public static $inject = ['$scope', BackendService.NAME];

        constructor($scope:IEntityStateDirectiveScope, backendService:BackendService) {
            $scope.changeEntityState = (nextState:number)=> {
                backendService.changeEntityState($scope.entity.id, nextState).success(state=> {
                    $scope.entity.state = state
                });
            };

            $scope.getStatusStatus = (entityState:EntityState)=>{
              if (!entityState){
                  return "";
              }
              if (entityState.name == "Tested")
                return "success";
              else if (entityState.name == "Reopen" || entityState.name == "Closed" ||  entityState.name == "Done")
                return "warning";
              else if (entityState.name == "In Dev" || entityState.name == "Testing" )
                return "default";
              else
                return "";
            }
        }
    }
}