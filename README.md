# gooddata-agent

A simple command line tool for uploading data to GoodData's WebDAV storage

## Usage

*Download:*

from http://gooddata.s3.amazonaws.com/agent/gdc-agent-0.9.jar

*Configure:*

Create and edit the properties file (e.g. "my.properties") based on the commented sample configuration file below. 

*Run:*

    java -jar target/gdc-agent-0.9.jar my.properties

Note: the following configuration parameters can be overriden using
command line parameters:
  * `jdbc.driver`: `--jdbc-driver`
  * `jdbc.driverPath`: `--jdbc-driverPath`
  * `jdbc.password`: `--jdbc-password`
  * `jdbc.url`: `--jdbc-url`
  * `jdbc.username`: `--jdbc-username`
  * `gdc.password`: `--gdc-password`
  * `gdc.username`: `--gdc-username`

For example:

    java -jar target/gdc-agent-0.8.jar --jdbc-username=admin --jdbc-password=e6bweH5lx67ygM45 my.properties

*Triggering server side ETL:*

The agent can optionally run a server side ETL process. It will
automatically send information about the uploaded file in the ETL
parameters: `gdc_agent_zip`/ `gdc_agent_manifest` and
`gdc_agent_zip_url_nocreds`/`gdc_agent_manifest_url_nocreds`.

Moreover, if the `gdc.etl.send_credentials` property is set to
`true`, the agent tool will also sent the following parameters:
`gdc_username`, `gdc_password` and `gdc_agent_zip_url` or
`gdc_agent_manifest_url`.

Note you can use the gdc_agent_zip_url or gdc_agent_manifest_url variable
in the FileDownloader CloudConnect component as it will include the full URL of the uploaded file including the
credentials, for example
`https://user%40company.com:P4$$w0rd@secure-di.gooddata.com/project-uploads/fqp21nrdqm666u5nwgl0upsgee4a4xv2/data-20141223161932.zip`.

In addition, you can provide custom ETL parameters using
property keys prefixed `gdc.etl.param.` or `gdc.etl.hidden_param.`

_Caution:_ never put any sensitive information directly into your
CloudConnect graph or into the workspace.prm CloudConnect configuration file.

*Sample Configuration:*

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
    #
    # Alternatively, you can upload uncompressed files individually and
    # reference them by a manifest file; the manifest is a text file including
    # each file name per line:
    # gdc.upload_manifest=manifest-${yyyyMMddHHmmss}.txt
    #
    # You can override the names of parameters that are used to send the
    # manifest or zip file names, timestamp and repords dir
    # gdc.etl.param_name.file=gdc_agent_zip
    # gdc.etl.param_name.manifest=gdc_agent_manifest
    # gdc.etl.param_name.now=gdc_agent_now
    # gdc.etl.param_name.gdc_username=gdc_username
    # gdc.etl.param_name.gdc_password=gdc_password
    # gdc.etl.param_name.url=gdc_agent_zip_url
    # gdc.etl.param_name.url.no_creds=gdc_agent_zip_url_nocreds
    # gdc.etl.param_name.manifest_url=gdc_agent_manifest_url
    # gdc.etl.param_name.manifest_url.no_creds=gdc_agent_manifest_url_nocreds
    # Remote directory where the ETL is expected to put any reports
    # about the execution.
    # gdc.etl.param_name.repords=gdc_agent_reports

    # If you want the agent to send credentials to the ETL process
    # set the following property to false. Turned off by default for
    # security reasons
    # gdc.etl.send_credentials=false

    # CloudConnect ETL configuration
    gdc.etl.process_url=https://secure.gooddata.com/gdc/projects/fqp21nrdqm666u5nwgl0upsgee4a4xv2/dataload/processes/5f4b4ca9-3f1f-4821-80b1-17322e831e40
    gdc.etl.graph=Test/graph/import_archive.grf
    gdc.etl.param.param1=a parameter
    gdc.etl.param.param2=yet another parameter
    gdc.etl.hidden_param.super_secret_parameter=pa$$w0rd01


*... or build from sources:*

    git clone git://github.com/koles/gooddata-agent.git
    cd gooddata-agent
    vim my.properties
    mvn assembly:assembly
    
    java -jar target/gdc-agent-0.1-SNAPSHOT-jar-with-dependencies.jar my.properties
