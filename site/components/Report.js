var React = require('react');

var _ = require('underscore');
var Date = require('./Date');
var Duration = require('./Duration');
var ProgressBar = require('./ProgressBar');

var Report = React.createClass({
	getInitialState: function(){
		return {
			expanded:false
		}
	},
    render: function(){
        var report = this.props.report;
        var subReports;
        if (report.subTasks && report.subTasks.length > 0) {
            subReports = _.map(report.subTasks, function(subTask){
                return <Report subReport={true} report={subTask} key={subTask.name + '_' + subTask.startTime} />
            });
        }
        var failedIcon = (<span className="report-icon failure-icon octicon octicon-issue-opened"></span>);
        var successIcon = (<span className="report-icon success-icon octicon octicon-check"></span>);
        var icon = report.failed ? failedIcon : successIcon;
        return (
            <div className="service">
                <div className="service-head" onClick={this.toggle}>
                	<span className="left spacer-right">{icon}{report.name}</span>
                	{!this.props.subReport && <span className="left"><Date timestamp={report.startTime} humanize={true} /></span>}
                    <span className="right"><Duration time={report.endTime - report.startTime} /></span>
                    <div className="reset"></div>
                </div>
                {this.props.inProgress && <ProgressBar total={report.totalTasks} current={report.tasksDone} />}
                {this.state.expanded && 
                <div className="service-content">
                    <ul className="no-style-type">
                        <li>Total:{report.totalTasks} task(s)</li>
                        <li>Started:<Date timestamp={report.startTime} /></li>
                        <li>Ended:<Date timestamp={report.endTime} /></li>
                    </ul>
                </div>
                }
                {this.state.expanded && subReports && 
                <div className="sub-reports">
                    <div className="important-text italic">Sub tasks:</div>
                    {subReports}
                </div>
                }
            </div>
        );
    },
    toggle: function(){
    	this.setState({expanded:!this.state.expanded});
    }
});

module.exports = Report;