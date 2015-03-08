var React = require('react');
var Date = require('./Date');


var ServiceHead = React.createClass({
    render: function(){
        var service = this.props.service;
        var latestVersion = service.versions[service.versions.length-1];
        var serviceIcon = "/assets/img/service-icon-"+service.type+".png";
        return (
            <div className="service-head" onClick={this.toggleContent}>
	            <div className="left spacer-right">
		        	<img src={serviceIcon} />
        		</div>
            	<div className="left">
                    {service.artifactId}
                </div>
                <div className="right latest-version">
                    Latest version: {latestVersion.name} (<Date timestamp={latestVersion.timestamp} />)
                </div>
                <div className="reset"></div>
            </div>
        );
    },
    toggleContent: function(){
        this.props.toggleService();
    }
});

module.exports = ServiceHead;