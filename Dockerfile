FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/clojure-image-bank.jar /clojure-image-bank/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clojure-image-bank/app.jar"]
