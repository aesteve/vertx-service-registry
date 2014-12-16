// FIXME : could use require instead of a global (don't forget to require jQuery & underscore)
// Main module
app = angular.module("VertxServices", []);
// Register directives

// Register controllers
app.controller("ServicesListCtrl", function($scope){
	$scope.services = [];
	$scope.filters = {
		tags:[],
		search:""
	};
	$scope.expanded = {};
	$scope.status = "Fetching";
	var init = function(){
		var def = $.ajax({
			url:"/api/1/services",
			method:"GET",
			dataType:"json",
			success:function(result){
				$scope.services = result;
				$scope.matchingServices = $scope.services;
				$scope.status = "Finished";
				$scope.$apply();
			},
			error:function(jqXhr, status, error){
				$scope.status = "Error";
			}
		});
	};
	
	var matches = function(value, criteria){
		return value.indexOf(criteria) > -1;
	};
	
	var anyPropMatches = function(map, criteria){
		var atLeastOneMatch = false;
		_.each(map, function(value){
			if(_.isString(value)){
				atLeastOneMatch = atLeastOneMatch || matches(value, criteria);
			} else if(_.isArray(value)){
				_.each(value, function(el){
					if(_.isString(el)) {
						atLeastOneMatch = atLeastOneMatch || matches(el, criteria);
					}
				});
			}
		});
		return atLeastOneMatch;
	};
	
	var containsTag = function(service, tags){
		var atLeastOneMatch = false;
		_.each(service.tags, function(tag){
			_.each(tags, function(tagFilter){
				if(tag == tagFilter) {
					atLeastOneMatch = true;
				}
			});
		});
		if(service.complementaryInfos && service.complementaryInfos["keywords"]){
			_.each(service.complementaryInfos["keywords"], function(keyword){
				_.each(tags, function(tag){
					if(keyword == tag) {
						atLeastOneMatch = true;
					}
				});
			});
		}
		return atLeastOneMatch;
	};
	
	$scope.updateMatchingServices = function(){
		$scope.matchingServices = _.filter($scope.services, function(service){
			var matching = true;
			var searchTxt = $scope.filters.search; 
			if(searchTxt)
			{
				matching = matches(service.groupId, searchTxt)
					|| matches(service.artifactId, searchTxt)
					|| anyPropMatches(service.complementaryInfos, searchTxt)
			}
			var tags = $scope.filters.tags;
			if(tags && tags.length > 0 && matching) {
				// must match tag
				matching = matching && containsTag(service, tags);
			}
			return matching;
		})
	};
	
	$scope.toggle = function(service){
		if($scope.expanded == service){
			$scope.expanded = {};
		} else {
			$scope.expanded = service;
		}
	};
	
	$scope.addTagToSearch = function(tag){
		$scope.filters.tags.push(tag);
		$scope.updateMatchingServices();
	};
	
	$scope.removeTagFromSearch = function(tag){
		var idx = $scope.filters.tags.indexOf(tag);
		if (idx > -1) {
			$scope.filters.tags.splice(idx, 1);
			$scope.updateMatchingServices();
		}
	};
	
	init();
});