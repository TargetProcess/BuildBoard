/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    interface IConfigControllerScope {
        obj:any;
        toggleMode():void
        save():void
    }

    export class ConfigController {
        public static NAME = "ConfigController";

        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME
        ];

        constructor(private $scope:IConfigControllerScope, $state:ng.ui.IStateService, backendService:BackendService) {
            var prevConfig;
            $scope.obj = {
                options: {
                    name: 'Build Board Config',
                    mode: 'form',
                    modes:['code','form']
                }
            };

            backendService.getConfig().then(data=> {
                prevConfig = data.data;
                $scope.obj.data = data.data;
            });

            $scope.toggleMode = ()=> {
                $scope.obj.options.mode = $scope.obj.options.mode == 'code' ? 'tree' : 'code';
            };

            $scope.save = ()=> {
                backendService.saveConfig($scope.obj.data).then(()=>{
                    $state.go('list')
                }, ()=>{
                    $scope.obj.data = prevConfig;
                    alert('Invalid config');
                })
            }
        }
    }
}