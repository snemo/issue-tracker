(function() {
    'use strict';
    angular
        .module('issueTrackerApp')
        .factory('Issue', Issue);

    Issue.$inject = ['$resource', 'DateUtils'];

    function Issue ($resource, DateUtils) {
        var resourceUrl =  'api/issues/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.created = DateUtils.convertLocalDateFromServer(data.created);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.created = DateUtils.convertLocalDateToServer(copy.created);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.created = DateUtils.convertLocalDateToServer(copy.created);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
