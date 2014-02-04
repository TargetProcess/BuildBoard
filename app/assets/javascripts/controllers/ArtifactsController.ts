/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IArtifactsScope extends ng.IScope {
        failedTests: TestCase[];
        testCasePackages: TestCasePackage[];
        totalCount: number;
        failedCount: number;
        skippedCount: number;
        totalTime: number;
        statusUrl: string;
        closeView():void;
    }

    export class ArtifactsController {
        public static NAME = "ArtifactsController";

        public static $inject = [
            '$scope',
            '$state',
            BackendService.NAME
        ];

        constructor(private $scope:IArtifactsScope, $state:ng.ui.IStateService, backendService:BackendService) {
            var nodeName = $state.params['buildNode'];
            var buildNumber = $state.params['build'];
            var $parentScope = <IBranchScope>this.$scope.$parent;
            var build = $parentScope.builds.filter(b => b.number == buildNumber)[0];
            var node = this.getBuildNode(build.node, nodeName);
            var testResults = node.artifacts.filter(a => a.name == 'testResults');
            this.$scope.statusUrl = node.statusUrl;
            if (testResults.length > 0) {
                backendService.getArtifact(testResults[0].url).success(testCasePackages => {
                    if (testCasePackages.length > 0) {
                        var testCasePackage = testCasePackages[0];
                        this.$scope.testCasePackages = this.getPackagesWithTests(testCasePackage);
                        this.$scope.failedTests = this.getFailedTests(this.$scope.testCasePackages);
                        //todo: refactor
                        this.$scope.totalCount = _.reduce(this.$scope.testCasePackages, function (sum, p) {
                            return sum + p.totalCount;
                        }, 0);
                        this.$scope.skippedCount = _.reduce(this.$scope.testCasePackages, function (sum, p) {
                            return sum + p.skippedCount;
                        }, 0);
                        this.$scope.failedCount = _.reduce(this.$scope.testCasePackages, function (sum, p) {
                            return sum + p.failedCount;
                        }, 0);
                        this.$scope.totalTime = _.reduce(this.$scope.testCasePackages, function (sum, p) {
                            return sum + p.duration;
                        }, 0);
                    }
                });
            }

            this.$scope.closeView = ()=> {
                $state.go("list.branch");
            };
        }

        getBuildNode(node:BuildNode, nodeName:string):BuildNode {
            if (node.name == nodeName) {
                return node;
            }
            else {
                var candidates = _.chain(node.children)
                    .map(child => this.getBuildNode(child, nodeName))
                    .filter(child => child != null)
                    .value();

                return candidates.length > 0
                    ? candidates[0]
                    : null;
            }
        }

        getFailedTests(packages:TestCasePackage[]):TestCase[] {
            return _.chain(packages)
                .map(p => p.testCases)
                .flatten()
                .filter(tc => tc.result == "Failure")
                .value();
        }

        getPackagesWithTests(tcPackage:TestCasePackage):TestCasePackage[] {
            var packages = _.chain(tcPackage.packages)
                .map(p => this.getPackagesWithTests(p))
                .flatten()
                .value();

            if (tcPackage.testCases.length > 0) {
                tcPackage.totalCount = this.getTestCasesCountByStatus(tcPackage, packages);
                tcPackage.failedCount = this.getTestCasesCountByStatus(tcPackage, packages, "Failure");
                tcPackage.skippedCount = this.getTestCasesCountByStatus(tcPackage, packages, "Ignored");
                tcPackage.passedCount = this.getTestCasesCountByStatus(tcPackage, packages, "Success");
                tcPackage.duration = this.getTestCasesDuration(tcPackage, packages);
                packages.push(tcPackage);
            }

            return packages.sort(p => p.name);
        }

        getTestCasesCountByStatus(tcPackage:TestCasePackage, children:TestCasePackage[], status:string = null):number {
            return this.getTestCasesCountByPredicate(tcPackage, children, tc => status == null || tc.result == status, tc => 1);
        }

        getTestCasesDuration(tcPackage:TestCasePackage, children:TestCasePackage[]):number {
            return this.getTestCasesCountByPredicate(tcPackage, children, tc => true, tc => tc.duration);
        }

        getTestCasesCountByPredicate<T>(tcPackage:TestCasePackage, children:TestCasePackage[], predicate:(tc:TestCase) => boolean, selector:(tc:TestCase) => number):number {
            var testCases = _.chain(children)
                .map(p => p.testCases)
                .flatten()
                .value()
                .concat(tcPackage.testCases);

            return testCases.filter(predicate)
                .reduce(function(sum, tc){
                    return sum + selector(tc);
                }, 0);
        }
    }
}