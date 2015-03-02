var React = require('react');
var Service = require('./Service');
var _ = require('underscore');

var Services = React.createClass({
	render: function(){
        // should be in getInitialState, but not working with nashorn
        if(!this.state)
            this.state = {};
        var instance_ = this;
        var services = _.map(this.props.services, function(service, idx){
            return (<Service key={"service_"+idx} service={service} expanded={instance_.props.expanded} />);
        });
        return (
            <div className="services">
                {services}
            </div>
        );
	}
});

module.exports = Services