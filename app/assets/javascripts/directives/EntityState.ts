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
            '<div onclick="this.focus();" onfocusin="this.focus();" ng-focus="clearTimeoutOnFocus();"  ng-blur="hideOnBlur();" tabindex="-1" class="dropdown open">',
            '<a href="" title="{{entity.state.name}}" ng-click="showList = !showList" class="status {{type}} {{getStatusStatus(entity.state)}} dropdown-toggle" data-toggle="dropdown">{{entity.state.name}}</a>',
            '<ul ng-if="showList" class="dropdown-menu dropdown-menu_state">',
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
        showList:boolean
        clearTimeoutOnFocus():void
        hideOnBlur():void
    }

    export class EntityStateDirectiveController {
        public static $inject = ['$scope', BackendService.NAME, '$timeout', '$q'];

        constructor($scope:IEntityStateDirectiveScope, backendService:BackendService, $timeout:ng.ITimeoutService, $q:ng.IQService) {
            var timeoutId = $q.defer().promise;
            $scope.clearTimeoutOnFocus = () => {
                $timeout.cancel(timeoutId);
            };
            $scope.hideOnBlur = () => {
                timeoutId = $timeout(() => {
                    $scope.showList = false;
                    $scope.$digest()
                }, 200);
            };

            $scope.changeEntityState = (nextState:number)=> {
                backendService.changeEntityState($scope.entity.id, nextState).success(state=> {
                    $scope.entity.state = state
                });
            };

            $scope.getStatusStatus = (entityState:EntityState)=> {
                if (!entityState) {
                    return "";
                }
                if (entityState.name == "Tested" || entityState.name == "Merged")
                    return "success";
                else if (entityState.name == "Reopen" || entityState.name == "Closed" || entityState.name == "Done")
                    return "warning";
                else if (entityState.name == "In Dev" || entityState.name == "Testing")
                    return "default";
                else
                    return "";
            }
        }
    }
}