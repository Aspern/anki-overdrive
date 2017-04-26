var restPostService = angular.module('PostService', [])
restPostService.factory('SendPostReq', ['$http', function ($http) {

    var restPostDataOp = {};

    restPostDataOp.sendPost = function (url,data) {
        return $http.post(url , data);
    };
    return restPostDataOp;

}]);