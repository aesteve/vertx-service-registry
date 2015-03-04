var React = require('react');

var Loader = require('./Loader');

var ReportPage = React.createClass({
    render:function(){
        return (
            <div>
                <h1>Services discovery reports</h1>
                <Loader/>
            </div>
        );
    }
});
        
module.exports = ReportPage;