FROM tomcat:10.1.24-jdk17

RUN rm -rf /usr/local/tomcat/webapps/*

COPY target/*.war /user/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS_"-Xms512 -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -Djava.security.egd=file:/dev/./urandom"