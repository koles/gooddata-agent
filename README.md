# gooddata-agent

A simple command line tool for uploading data to GoodData's WebDAV storage

## Usage

`java -jar gooddata-agent.jar my.properties`

*Configuration:*

    ##############################
    ### GoodData configuration ###
    ##############################

    # Credentials
    gdc.username=pavel.kolesnikov@gooddata.com
    gdc.password=this is not my real password

    # Target
    gdc.upload_url=https://secure-di.gooddata.com/project-uploads/fqp21nrdqm666u5nwgl0upsgee4a4xv2/
    gdc.upload_archive=data-${yyyyMMddHHmmss}.zip

    ######################################
    ### Source data on the file system ###
    ######################################

    # include all CSV files from that directory in the zip file
    filesystem.input_dir=/tmp/exports/to_gooddata
    filesystem.wildcard=*.csv

## TODO

* Start a CloudConnect graph upon a successful upload
* Pull source data from a database via JDBC
