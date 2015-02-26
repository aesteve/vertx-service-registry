// FIXME : scan for nested folders when building, so that we can require('./service/Head')

var ServiceHead = require ('ServiceHead');
var ServiceContent = require ('ServiceContent');
var ServiceFoot = require ('ServiceFoot');






var Service = React.createClass({
    render: function(){
        // should be in getInitialState, but not working with nashorn
        if(!this.state)
            this.state = {};
        
        return (
            <div className="service">
                <ServiceHead service={this.props.service} />
                <ServiceContent service={this.props.service} />
                <ServiceFoot service={this.props.service} />
            </div>
        );
    }
});

module.exports = Service;