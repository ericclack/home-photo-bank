FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/home-photo-bank.jar /home-photo-bank/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/home-photo-bank/app.jar"]
