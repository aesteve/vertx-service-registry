var React = require('react');

var Pagination = React.createClass({
    render:function(){
        var pagination = this.props.pagination;
        if (!pagination) {
            return (<div className="pagination-bar"></div>);
        }
        var first = pagination.first;
        var prev = pagination.prev;
        var next = pagination.next;
        var last = pagination.last;
        return (
            <ul className="pagination-bar flat-list">
                {first && <li className="page-link inline-block"><a>&#8592;First</a></li>}
                {prev && <li className="page-link inline-block"><a>{pagination.current - 1}</a></li>}
                <li className="current-page inline-block"><a>{pagination.current}</a></li>
                {next && <li className="page-link inline-block"><a>{pagination.current + 1}</a></li>}
                {last && <li className="page-link inline-block"><a>Last &#8594;</a></li>}
            </ul>
        );
    }
});

module.exports = Pagination;