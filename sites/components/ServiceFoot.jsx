var Version = require('version');
var TagFoot = require('TagFoot');

var ServiceFoot = React.createClass({
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
                <Version name={version.name} />
            );
        });
        return (
            <div className="service-foot">
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