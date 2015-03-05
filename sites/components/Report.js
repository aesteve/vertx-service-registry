var React = require('react');

var _ = require('underscore');
var Date = require('./Date');
var Duration = require('./Duration');

var Report = React.createClass({
    render: function(){
        var report = this.props.report;
        var subReports;
        if (report.subTasks && report.subTasks.length > 0) {
            subReports = _.map(report.subTasks, function(subTask){
                return <Report report={subTask} key={report.name + '_' + report.startTime} />
            });
        }
        return (
            <div className="service">
                <div className="service-head">
                    <span className="left">{report.name}</span>
                    <span className="right"><Duration time={report.endTime - report.startTime} /></span>
                    <div className="reset"></div>
                </div>
                <div className="service-content">
                    <ul className="no-style-type">
                        <li><Date timestamp={report.startTime} /></li>
                        <li><Date timestamp={report.endTime} /></li>
                    </ul>
                </div>
                {subReports && 
                <div className="sub-reports">
                    <div>Sub tasks</div>
                    {subReports}
                </div>
                }
            </div>
        );
    }
});

module.exports = Report;