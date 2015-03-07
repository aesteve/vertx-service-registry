var React = require('react');

var SearchBar = require('./SearchBar');
var Services = require('./Services');
var Pagination = require('./Pagination');
var BackToTop = require('./BackToTop');
var ServicesCollection = require('../model/PaginatedCollection');
var _ = require('underscore');


var DEFAULT_API_VERSION = 2;

var servicesCollection = new ServicesCollection(DEFAULT_API_VERSION, "services");

var App = React.createClass({
	componentDidMount: function(){
		if (this.props.services) {
			return;
		}
		servicesCollection.setCurrentFromUrl();
		this.fetchServices();
	},
	render: function () {
		var expanded = this.props.renderedOnServer;
		var filters = {
				textSearch:"",
				tags:[],
				sort:undefined
		};
		if (!this.state){
			if (this.props.filters){
				filters = this.props.filters;
			}
			this.state = {
					fetchInProgress: false,
					filters:filters,
					apiVersion:servicesCollection.apiVersion
			};
		}
		var services = this.state.matchingServices || this.props.matchingServices || this.props.services;
		var pagination = this.props.pagination || servicesCollection.pagination;
		return (
				<div className="webapp">
				{!this.props.renderedOnServer && <SearchBar fetchInProgress={this.state.fetchInProgress} filters={this.state.filters} filtersChanged={this.filtersChanged} apiVersion={this.state.apiVersion} apiVersionChanged={this.apiVersionChanged} />}
				<Pagination navigate={this.navigate} pagination={pagination} />
				<Services services={services} expanded={expanded} />
				<Pagination navigate={this.navigate} pagination={pagination} />
				<BackToTop />
				</div>
		);
	},
	fetchServices: function(){
		var instance_ = this;
		this.setState({fetchInProgress:true});
		servicesCollection.filters = this.state.filters;
		servicesCollection.fetch().done(function(){
			instance_.refreshServicesList();
		});
	},
	refreshServicesList: function(){
		this.setState({matchingServices:servicesCollection.currentPageResources, fetchInProgress:false});
	},
	filtersChanged: function(newFilters){
		var instance_ = this;
		servicesCollection.reset();
		this.setState({filters:newFilters}, function(){
			instance_.fetchServices();
		});
	},
	navigate: function(rel){
		var instance_ = this;
		this.setState({fetchInProgress:true});
		servicesCollection.goTo(rel).done(function(){
			instance_.refreshServicesList();
		});
	},
	apiVersionChanged: function(apiVersion){
		servicesCollection.setApiVersion(apiVersion);
		servicesCollection.reset();
		var instance_ = this;
		this.setState({apiVersion:apiVersion, filters:{textSearch:"", tags:[], sort:undefined}}, function(){
			servicesCollection.filters = instance_.state.filters;
			if (apiVersion == 3)
				alert("NIY");
			servicesCollection.fetch().always(function(){
				instance_.refreshServicesList();
			});
		});
	}
});

module.exports = App;