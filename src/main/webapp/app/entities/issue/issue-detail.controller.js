(function() {
    'use strict';

    angular
        .module('issueTrackerApp')
        .controller('IssueDetailController', IssueDetailController);

    IssueDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'Issue', 'User'];

    function IssueDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, Issue, User) {
        var vm = this;

        vm.issue = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('issueTrackerApp:issueUpdate', function(event, result) {
            vm.issue = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
