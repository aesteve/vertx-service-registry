var React = require('react');

var SearchBar = require('./SearchBar');
var Services = require('./Services');
var Pagination = require('./Pagination');
var BackToTop = require('./BackToTop');
var ServicesCollection = require('../model/ServicesCollection');
var _ = require('underscore');


var DEFAULT_API_VERSION = 2;

var servicesCollection = new ServicesCollection(DEFAULT_API_VERSION);

var App = React.createClass({
    getInitialState: function(){
        return {
            fetchInProgress: false,
            filters:{
                textSearch:"",
                tags:[],
                sort:undefined
            },
            apiVersion:servicesCollection.apiVersion
        };
    },
    componentDidMount: function(){
        if (this.props.services) {
            return;
        }
        this.fetchServices();
    },
	render: function () {
        var services = this.state.matchingServices || this.props.matchingServices || this.props.services;
        console.log("services:");
        _.each(this.props, function(prop){
            console.log(prop);
        });
		return (
            <div className="webapp">
                <SearchBar filters={this.state.filters} filtersChanged={this.filtersChanged} apiVersion={this.state.apiVersion} apiVersionChanged={this.apiVersionChanged} />
                <Pagination navigate={this.navigate} pagination={servicesCollection.pagination} />
                <Services services={services} />
                <Pagination navigate={this.navigate} pagination={servicesCollection.pagination} />
                <BackToTop />
            </div>
        );
	},
    fetchServices: function(){
        var instance_ = this;
        servicesCollection.filters = this.state.filters;
        servicesCollection.fetch().done(function(){
            instance_.refreshServicesList();
        });
    },
    refreshServicesList: function(){
        this.setState({matchingServices:servicesCollection.currentPageServices, fetchInProgress:false});
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