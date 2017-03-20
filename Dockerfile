FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/clojure-photo-bank.jar /clojure-photo-bank/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clojure-photo-bank/app.jar"]
