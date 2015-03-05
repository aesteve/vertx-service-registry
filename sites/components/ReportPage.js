var React = require('react');

var Loader = require('./Loader');
var _ = require('underscore');
var Report = require('./Report');
var ReportsCollection = require('../model/PaginatedCollection');

var reportsCollection = new ReportsCollection("2", "report");


var ReportPage = React.createClass({
    componentDidMount: function(){
        var instance_ = this;
        reportsCollection.fetch().done(function(){
            instance_.refreshReports();
        });
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
                {reports}
            </div>
        );
    }
});
        
module.exports = ReportPage;