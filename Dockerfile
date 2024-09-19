FROM tomcat:10.1.26-jdk17-temurin-noble

COPY target/fhir-facade-cnes-organization-pre-registration.war $CATALINA_HOME/webapps/ROOT.war

RUN mkdir -p "/data"
RUN mkdir -p "/logs"

# Configure the server to accept specific characters in URLs.
# The configuration is inserted on line 74. For using with different images, it should be revised.
RUN sed -i "74i relaxedQueryChars='^{}[]|&quot;'" $CATALINA_HOME/conf/server.xml

EXPOSE 8080:8080
