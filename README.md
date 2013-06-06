# gooddata-agent

A simple command line tool for uploading data to GoodData's WebDAV storage

## Usage

*Download:*

from http://gooddata.s3.amazonaws.com/agent/gdc-agent-0.1.jar

*Run:*

    java -jar target/gdc-agent-0.1.jar my.properties

*Configuration:*

    ##########################################
    ### Source data on a local file system ###
    ##########################################

    # include all CSV files from that directory in the zip file
    filesystem.input_dir=/tmp/exports/to_gooddata
    filesystem.wildcard=*.csv

    #################################
    ### Source data in a database ###
    #################################
    jdbc.driver_path=/usr/share/java/mysql-connector-java-5.1.5.jar
    jdbc.driver=com.mysql.jdbc.Driver
    jdbc.username=extracts
    jdbc.password=*******
    jdbc.url=jdbc:mysql://localhost/test

    # TODO support for incremental upload
    data.user.sql=SELECT id,name,email FROM user
    data.message.sql=SELECT id,title,category,author_id,posted FROM user WHERE deleted IS NULL

    ##############################
    ### GoodData configuration ###
    ##############################

    # Credentials
    gdc.username=my.username@company.com
    gdc.password=my GoodData password

    # Target
    gdc.upload_url=https://secure-di.gooddata.com/project-uploads/fqp21nrdqm666u5nwgl0upsgee4a4xv2/
    gdc.upload_archive=data-${yyyyMMddHHmmss}.zip

    # CloudConnect ETL configuration
    gdc.etl.process_url=https://secure.gooddata.com/gdc/projects/fqp21nrdqm666u5nwgl0upsgee4a4xv2/dataload/processes/5f4b4ca9-3f1f-4821-80b1-17322e831e40
    gdc.etl.graph=Test/graph/import_archive.grf
    gdc.etl.param.param1=a parameter
    gdc.etl.param.param2=yet another parameter
    # The name of the CloudConnect parameter used to pass the file name (e.g. data-20130606135445.zip)
    # "file" is the default value (i.e., the following line is actually redundant)
    gdc.etl.param_name.file=file


*... or build from sources:*

    git clone git://github.com/koles/gooddata-agent.git
    cd gooddata-agent
    vim my.properties
    mvn assembly:assembly
    
    java -jar target/gdc-agent-0.1-SNAPSHOT-jar-with-dependencies.jar my.properties

## TODO

* Pull source data from a database via JDBC using the incremental mode
