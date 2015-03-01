var React = require('react');
var Version = require('./Version');
var TagFoot = require('./TagFoot');
var _ = require('underscore');

var ServiceFoot = React.createClass({
    getInitialState: function(){
        return {
            expanded: this.props.expanded
        }
    },
    componentWillReceiveProps: function(newProps){
        this.setState({expanded:newProps.expanded});
    },
    render: function(){
        var service = this.props.service;
        var tags = service.tags;
        var jsxTags = _.map(tags, function(tag, idx){
            return (
                <TagFoot tag={tag} key={"tag_"+idx} />
            );// FIXME : add click handlers
        });
        var infos = service.complementaryInfos;
        
        if (infos && infos['keywords'] && infos['keywords'].length > 0) {
            keywords = _.map(infos['keywords'], function(keyword, idx){
                return (
                    <TagFoot tag={keyword} key={"complInfo_"+idx} />
                );
            });
        }
        var versions = _.map(service.versions, function(version){
            return (
                <Version key={version.name} name={version.name} />
            );
        });
        var className = "service-foot ";
        if (!this.state.expanded) {
            className += "hidden ";
        }
        return (
            <div className={className}>
                <div className="left">
                    Tags : 
                    <ul className="flat-list no-style-type">
                        {jsxTags}
                        {keywords}
                    </ul>
                </div>
                <div className="right">
                    Versions:
                    <ul className="flat-list no-style-type">
                        {versions}
                    </ul> 
                </div>
                <div className="reset"></div>
            </div>
        );
    }
});

module.exports = ServiceFoot;