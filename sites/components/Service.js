var React = require('react');

var ServiceHead = require ('./ServiceHead');
var ServiceContent = require ('./ServiceContent');
var ServiceFoot = require ('./ServiceFoot');






var Service = React.createClass({
    getInitialState: function(){
        return {
            expanded: false
        }
    },
    render: function(){
        return (
            <div className="service">
                <ServiceHead service={this.props.service} toggleService={this.toggleService} />
                <ServiceContent service={this.props.service} expanded={this.state.expanded} />
                <ServiceFoot service={this.props.service} expanded={this.state.expanded} />
            </div>
        );
    },
    toggleService:function(){
        this.setState({expanded:!this.state.expanded});
    }
});

module.exports = Service;