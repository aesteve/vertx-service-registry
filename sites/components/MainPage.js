var React = require('react');

var SearchBar = require('./SearchBar');
var Services = require('./Services');
var Pagination = require('./Pagination');
var ServicesCollection = require('../model/ServicesCollection');

var servicesCollection = new ServicesCollection();

var App = React.createClass({
    getInitialState: function(){
        return {
            services:this.props.services
        };
    },
    componentDidMount: function(){
        var instance_ = this;
        servicesCollection.fetch().done(function(){
            instance_.refreshServices();
        });
    },
	render: function () {
        var services = this.state.matchingServices || this.props.matchingServices || this.props.services;
        var textSearch = "";
        if (this.state.filters && this.state.filters.text) 
            textSearch = this.state.filters.text;
        var sort;
        if (this.state.filters && this.state.filters.sort)
            sort = this.state.filters.sort;
		return (
            <div className="webapp">
                <SearchBar />
                <Pagination pagination={servicesCollection.pagination} />
                <Services services={services} />
                <Pagination pagination={servicesCollection.pagination} />
            </div>
        );
	},
    refreshServices: function(){
        this.setState({matchingServices:servicesCollection.currentPageServices});
    }
});

module.exports = App;