var React = require('react');

var Loader = require('./Loader');
var _ = require('underscore');
var Report = require('./Report');
var SockJS = require('sockjs-client')
var ReportsCollection = require('../model/PaginatedCollection');

var reportsCollection = new ReportsCollection("2", "report");

var socket;

var ReportPage = React.createClass({
    componentDidMount: function(){
        var instance_ = this;
        reportsCollection.fetch().done(function(){
            instance_.refreshReports();
        });
        socket = new SockJS("localhost:8080/sockets");
        socket.onmessage = function (message) {
        	var report = JSON.parse(message.data);
        	instance_.setState({reportInProgress:report});
        };
    },
    componentWillUnmount: function(){
    	if (socket) {
    		socket.close();
    	}
    },
    refreshReports: function(){
        this.setState({reports:reportsCollection.currentPageResources});
    },
    getInitialState: function(){
        return {
            reports:{}
        };
    },
    render:function(){
        var reports = _.map(this.state.reports, function(report){
            return (
                <Report key = {report.name + '_' + report.startTime} report={report} />
            );
        });
        return (
            <div>
                <h1>Services discovery reports</h1>
                {this.state.reportInProgress && 
                <div className="task-in-progress">
                	In progress:
                	<Report report={this.state.reportInProgress} inProgress={true} />
                	<hr />
                </div>
                }
                {reports}
            </div>
        );
    }
});
        
module.exports = ReportPage;