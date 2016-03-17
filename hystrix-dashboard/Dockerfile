FROM netflixoss/tomcat:7

RUN wget -q http://repo1.maven.org/maven2/com/netflix/turbine/turbine-web/1.0.0/turbine-web-1.0.0.war &&\
  mkdir ROOT &&\
  cd ROOT &&\
  jar xf ../turbine-web-1.0.0.war &&\
  cd .. &&\
  mv ROOT /tomcat/webapps/ROOT &&\
  rm turbine-web-1.0.0.war

EXPOSE 8080

ENTRYPOINT ["/tomcat/bin/catalina.sh"]

CMD ["run"]