var React = require('react');

var SearchBar = require('./SearchBar');
var Services = require('./Services');
var Pagination = require('./Pagination');
var ServicesCollection = require('../model/ServicesCollection');

var servicesCollection = new ServicesCollection();

var App = React.createClass({
    getInitialState: function(){
        return {
            services:this.props.services,
            fetchInProgress: false,
            filters:{
                textSearch:"",
                tags:[],
                sort:undefined
            }
        };
    },
    componentDidMount: function(){
        this.fetchServices();
    },
	render: function () {
        var services = this.state.matchingServices || this.props.matchingServices || this.props.services;
		return (
            <div className="webapp">
                <SearchBar filters={this.state.filters} filtersChanged={this.filtersChanged} />
                <Pagination pagination={servicesCollection.pagination} />
                <Services services={services} />
                <Pagination pagination={servicesCollection.pagination} />
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
        console.log(newFilters);
        var instance_ = this;
        this.setState({filters:newFilters});
        this.fetchServices();
    }
});

module.exports = App;