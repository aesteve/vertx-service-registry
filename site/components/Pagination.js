var React = require('react');
var $ = require('jquery');

var Pagination = React.createClass({
    componentWillReceiveProps:function(newProps){
        this.setState({pagination:newProps.pagination});
    },
    render:function(){
        if(!this.state){
            this.state = {
                pagination:this.props.pagination
            };
        }
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
                {first && <li className="page-link inline-block"><a href={this.getLink('first')} data-link="first" onClick={this.changePage} className="first-page-link">First</a></li>}
                {displayBefore && <li className="inline-block"><a className="fake-link">...</a></li>}
                {prev && <li className="page-link inline-block"><a href={this.getLink('prev')} data-link="prev" onClick={this.changePage}>{parseInt(pagination.current) - 1}</a></li>}
                <li className="current-page inline-block"><a>{pagination.current}</a></li>
                {next && <li className="page-link inline-block"><a href={this.getLink('next')} data-link="next" onClick={this.changePage}>{parseInt(pagination.current) + 1}</a></li>}
                {displayAfter && <li className="inline-block"><a className="fake-link">...</a></li>}
                {last && <li className="page-link inline-block"><a href={this.getLink('last')} data-link="last" onClick={this.changePage} className="last-page-link">Last</a></li>}
            </ul>
                    
        );
    },
    changePage: function(event){
       this.props.navigate($(event.target).attr("data-link"));
        event.preventDefault();
    },
    getLink: function(link){
        var pagination = this.state.pagination;
        var page = pagination.current;
        switch(link){
            case 'first':
                page=1;
                break;
            case 'prev':
                page=page-1;
                break;
            case 'next':
                page=page+1;
                break;
            case 'last':
                page=pagination.last;
                break;
        }
        var url = "?page="+page;
        if (pagination.perPage) {
            url += "&perPage="+pagination.perPage;
        }
        return url;
    }
});

module.exports = Pagination;