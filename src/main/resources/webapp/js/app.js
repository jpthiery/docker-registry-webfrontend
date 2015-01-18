var dockerRegistryApp = angular.module('dockerRegistryApp', ['ui.bootstrap']);
dockerRegistryApp.filter('bytes', function() {
    return function(bytes, precision) {
        if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
        if (typeof precision === 'undefined') precision = 1;
        var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
        number = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
    }
});
dockerRegistryApp.service('configurationService', ['$http', '$q', function($http, $q){
    //window.localStorage.removeItem('DockerFrontWebConfiguration');
    this.getConfiguration = function(){
        var deferred = $q.defer();
        if (window.localStorage.getItem('DockerFrontWebConfiguration') == null) {
            $http({
                method: 'GET',
                url: '/api/configuration'
            }).success(function(response) {
                window.localStorage.setItem('DockerFrontWebConfiguration',angular.toJson(response));
                deferred.resolve(response);
            }).error(function(data, status) {
                deferred.reject('Unable to retrive configuration : ' + status);
            });

        } else {
            var stored = window.localStorage.getItem('DockerFrontWebConfiguration');
            deferred.resolve(angular.fromJson(stored));
        }
        return deferred.promise;
    };
}]);

dockerRegistryApp.service('registryService', ['$http', '$q', '$log', 'configurationService', function($http, $q, $log, configurationService){

    this.search = function(terms) {
        var res = $q.defer();
        var urn = '/api/search';
        if (terms != null) {
            urn = urn + '?q=' + terms;
        }
        $http({
            method: 'GET',
            url: urn
        }).success(function(data){
            res.resolve(data);
        }).error(function(reason) {
            $log.error("Unable to done search request.")
        });
        return res.promise;
    }

    this.getImageDetail = function(name) {

        $log.info('getImageDetail ' + name);
        var res = $q.defer();
        $http({
            method: 'GET',
            url: '/api/image/' + name
        }).then(function(results, status, headers, config){
            for (i = 0; i < results.data.length; i++) {
                var current = results.data[i];
                if (current != null) {
                    res.resolve(current.response);
                    break;
                }
            }
        }, function(reason, status) {
            $log.error("Fail to get data for images " + name + " Reason : " + reason + " Status " +status);
        });
        return res.promise;
    };

}]);

dockerRegistryApp.controller('DockerConfigurationCtrl', ['configurationService', '$scope', function(configurationService, $scope){
    configurationService.getConfiguration().then(function(configuration){
        $scope.configuration = configuration;
    });
}]);

dockerRegistryApp.controller('DockerImagesCtrl', ['registryService', '$scope', function(registryService, $scope) {

    $scope.search = function(name) {
        registryService.search(name).then(function(results) {
                var images = [];
                for (i = 0; i < results.length; i++ )  {
                    var current = results[i];

                    images = images.concat(current.response.results);
                }
                $scope.images = images;
            });
    }


}]);


dockerRegistryApp.controller('DockerImagesInfo', ['registryService', '$scope' , function(registryService, $scope) {
    if ($scope.imageDetails == null) {
        $scope.imageDetails = [];
    }
    $scope.$watch('status.open', function(opened) {
        if (opened && !$scope.imageDetails[$scope.image.name]) {
            registryService.getImageDetail($scope.image.name).then(function(result){
                $scope.imagedetail = result;
                console.log("Get detail for image " + $scope.image.name + " id : " + result.id);
            });
        }
    });

}]);