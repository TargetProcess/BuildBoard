/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IArtifactsScope extends ng.IScope {
        buildNode(): BuildNode;
        failedTests(): TestCase[];
        testCasePackages(): TestCasePackage[];
        totalCount(): number;
        failedCount(): number;
        skippedCount(): number;
        totalTime(): number;
        logsUrl: string;
        closeView():void;
    }

    export class RunController {
        public static NAME = "RunController";

        public static $inject = [
            '$scope',
            '$state',
            ModelProvider.NAME
        ];

        constructor(private $scope:IArtifactsScope, $state:ng.ui.IStateService, private modelProvider:ModelProvider) {

            this.$scope.buildNode = () => {
                var branch = modelProvider.findBranch($state.params['name']);
                var build = Branch.findBuild(branch, $state.params['build']);

                return Build.findBuildNode(build, $state.params['run'], $state.params['part']);
            };
            this.$scope.testCasePackages = () => {
               var buildNode = this.$scope.buildNode();
                if (!buildNode || !buildNode.testResults){
                    return [];
                }

                return _.chain(buildNode.testResults)
                    .map(p => this.getPackagesWithTests(p))
                    .flatten()
                    .value();
            };
            this.$scope.failedTests = () => this.getFailedTests(this.$scope.testCasePackages());
            this.$scope.totalCount = () => _.reduce(this.$scope.testCasePackages(), function (sum, p) {
                return sum + p.totalCount;
            }, 0);
            this.$scope.skippedCount = () => _.reduce(this.$scope.testCasePackages(), function (sum, p) {
                return sum + p.skippedCount;
            }, 0);
            this.$scope.failedCount = () => _.reduce(this.$scope.testCasePackages(), function (sum, p) {
                return sum + p.failedCount;
            }, 0);
            this.$scope.totalTime = () => _.reduce(this.$scope.testCasePackages(), function (sum, p) {
                return sum + p.duration;
            }, 0);

            this.$scope.closeView = ()=> {
                $state.go("list.branch");
            };
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
                .reduce(function (sum, tc) {
                    return sum + selector(tc);
                }, 0);
        }
    }
}