var React = require('react');

var _ = require('underscore');
var Date = require('./Date');
var Duration = require('./Duration');
var ProgressBar = require('./ProgressBar');

var Report = React.createClass({
    render: function(){
        var report = this.props.report;
        var subReports;
        if (report.subTasks && report.subTasks.length > 0) {
            subReports = _.map(report.subTasks, function(subTask){
                return <Report report={subTask} key={subTask.name + '_' + subTask.startTime} />
            });
        }
        var failedIcon = (<span className="report-icon failure-icon octicon octicon-issue-opened"></span>);
        var successIcon = (<span className="report-icon success-icon octicon octicon-check"></span>);
        var icon = report.failed ? failedIcon : successIcon;
        return (
            <div className="service">
                <div className="service-head">
                	<span className="left">{icon}{report.name}</span>
                    <span className="right"><Duration time={report.endTime - report.startTime} /></span>
                    <div className="reset"></div>
                </div>
                {this.props.inProgress && <ProgressBar total={report.totalTasks} current={report.tasksDone} />}
                <div className="service-content">
                    <ul className="no-style-type">
                        <li>Started:&nbsp;<Date timestamp={report.startTime} /></li>
                        <li>Ended:&nbsp;&nbsp;<Date timestamp={report.endTime} /></li>
                        <li>Total:{report.totalTasks} task(s)</li>
                    </ul>
                </div>
                {subReports && 
                <div className="sub-reports">
                    <div className="important-text italic">Sub tasks:</div>
                    {subReports}
                </div>
                }
            </div>
        );
    }
});

module.exports = Report;