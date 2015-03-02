var React = require('react');

var ServiceHead = require ('./ServiceHead');
var ServiceContent = require ('./ServiceContent');
var ServiceFoot = require ('./ServiceFoot');






var Service = React.createClass({
    render: function(){
        console.log("EXPANDED MAIN = "+this.props.expanded);
        var expanded = this.props.expanded;
        if (this.state){
            expanded = this.state.expanded;
        }
        return (
            <div className="service">
                <ServiceHead service={this.props.service} toggleService={this.toggleService} />
                <ServiceContent service={this.props.service} expanded={expanded} />
                <ServiceFoot service={this.props.service} expanded={expanded} />
            </div>
        );
    },
    toggleService:function(){
        var expanded = this.props.expanded;
        if (this.state) {
            expanded = this.state.expanded;
        }
        this.setState({expanded:!expanded});
    }
});

module.exports = Service;