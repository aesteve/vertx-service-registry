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
        socket = new SockJS("/sockets");
        socket.onmessage = function (message) {
        	var report = JSON.parse(message.data);
        	if (report.endTime) {
        		instance_.state.reports.push(report);
        		instance_.replaceState({reports:instance_.state.reports});
        		instance_.forceUpdate();
        		console.log("forceUpdate");
        	} else {
        		instance_.setState({reportInProgress:report});        		
        	}
        	
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
    	var reports = _.sortBy(this.state.reports, function(report){
    		return - report.startTime;
    	});
        var reports = _.map(reports, function(report){
            return (
                <Report key = {report.name + '_' + report.startTime} report={report} />
            );
        });
        var displayProgress = this.state.reportInProgress && !this.state.reportInProgress.endTime;
        return (
            <div>
                <h1>Services discovery reports</h1>
                {this.state.reportInProgress && 
                <div className="task-in-progress">
                	<h2>In progress:</h2>
                	<Report report={this.state.reportInProgress} inProgress={true} />
                	<hr />
                </div>
                }
                <div className="past-reports">
	                <h2>Past reports</h2>
	                {reports}
                </div>
            </div>
        );
    }
});
        
module.exports = ReportPage;