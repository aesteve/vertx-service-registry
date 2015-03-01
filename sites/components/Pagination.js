var React = require('react');
var $ = require('jquery');

var Pagination = React.createClass({
    getInitialState:function(){
        return {
            pagination:this.props.pagination
        }
    },
    componentWillReceiveProps:function(newProps){
        this.setState({pagination:newProps.pagination});
    },
    render:function(){
        var pagination = this.state.pagination;
        if (!pagination) {
            return (<div className="pagination-bar"></div>);
        }
        var first = pagination.first;
        var prev = pagination.prev;
        var next = pagination.next;
        var last = pagination.last;
        var nbPagesDisplayed = 1;
        var displayBefore = pagination.current > 2;
        var displayAfter = pagination.total > pagination.current +1;
        return (
            <ul className="pagination-bar flat-list">
                {first && <li className="page-link inline-block"><a data-link="first" onClick={this.changePage}>&#8592;First</a></li>}
                {displayBefore && <li className="inline-block"><a className="fake-link">...</a></li>}
                {prev && <li className="page-link inline-block"><a data-link="prev" onClick={this.changePage}>{parseInt(pagination.current) - 1}</a></li>}
                <li className="current-page inline-block"><a>{pagination.current}</a></li>
                {next && <li className="page-link inline-block"><a data-link="next" onClick={this.changePage}>{parseInt(pagination.current) + 1}</a></li>}
                {displayAfter && <li className="inline-block"><a className="fake-link">...</a></li>}
                {last && <li className="page-link inline-block"><a data-link="last" onClick={this.changePage}>Last &#8594;</a></li>}
            </ul>
                    
        );
    },
    changePage: function(event){
       this.props.navigate($(event.target).attr("data-link"));
    }
});

module.exports = Pagination;