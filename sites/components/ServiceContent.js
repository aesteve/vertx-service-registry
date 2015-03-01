var React = require('react');
var ComplementaryInfos = require('./ComplementaryInfos');

var ServiceContent = React.createClass({
    render: function(){
        var service = this.props.service;
        return (
            <div className="service-main-content">
                <table className="no-style-type left service-infos">
                    <tbody>
                        <tr>
                            <td>Group id:</td>
                            <td>{service.groupId}</td>
                        </tr>
                        <tr>
                            <td>Artifact id:</td>
                            <td>{service.artifactId}</td>
                        </tr>
                    </tbody>
                    {service.complementaryInfos && <ComplementaryInfos infos={service.complementaryInfos} />}
                </table>
                <div className="reset"></div>
            </div>
        );
    }
});

module.exports = ServiceContent;