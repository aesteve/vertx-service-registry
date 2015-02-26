var ComplementaryInfos = React.createClass({
    render: function(){
        var infos = this.props.infos;
        var licenses = infos['licenses'];
        var licensesTags = "Unknown";
        if (licenses && licenses.length > 0) {
            var licensesTags = _.map(licenses, function(license, idx){
                return (<span key={"license_"+idx} className="license">{license}</span>);
            });
        }
        return (
        <tfoot>
            <tr>
                <td>Description:</td>
                <td>{service.complementaryInfos['description']}</td>
            </tr>
            <tr>
                <td>Author:</td>
                <td>{service.complementaryInfos["author"]}</td>
            </tr>
            <tr>
                <td>Homepage:</td>
                <td>
                    <a href="{service.complementaryInfos['homepage']}">{service.complementaryInfos['homepage']}</a>
                </td>
            </tr>
            <tr>
                <td>Licenses:</td>
                <td>
                    {licensesTags}
                </td>
            </tr>
        </tfoot>
        );
    }
});
    
module.exports = ComplementaryInfos;