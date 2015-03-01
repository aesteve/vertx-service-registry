var React = require('react');
var $ = require('jquery');

var BackToTop = React.createClass({
    getInitialState: function(){
        return {
            scrolled:false
        };
    },
    componentDidMount: function(){
        $('<img src="http://static.jboss.org/common/images/top.png" />').load(); // preload
        var instance_ = this;
        $(window).on("scroll", function(){
            if (instance_.isScrolled() != instance_.state.scrolled){
                instance_.setState({scrolled:instance_.isScrolled()});
            }
        });
    },
    render:function(){
        var className = "back-to-top ";
        if (!this.state.scrolled){
            className+= "hidden";
        }
        return (
            <div className={className}>
                <a href="#top" className="top-link"></a>
            </div>
        );
    },
    isScrolled: function(){
        return $(window).scrollTop() > 0;
    }
});

module.exports = BackToTop;