var Service = require('Service');

var Services = React.createClass({
	render: function(){
        // should be in getInitialState, but not working with nashorn
        if(!this.state)
            this.state = {};
        
        var services = _.map(this.props.services, function(service, idx){
            return (<Service key={"service_"+idx} service={service} />);
        });
        return (
            <div className="services">
                {services}
            </div>
        );
	}
});

module.exports = Services