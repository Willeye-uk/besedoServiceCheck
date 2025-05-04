# besedoServiceCheck

run 
mvn compile
mvn package
java -jar target/BesedoServiceCheck-1.0-SNAPSHOT-jar-with-dependencies.jar

or 

docker build -t health-checker:1.0 .
docker run health-checker:1.0