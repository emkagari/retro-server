# The login server: javac over login/src, the jars of login/lib and login/libs.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY login/ ./
RUN mkdir -p build/classes \
 && find src -name '*.java' > /tmp/sources.txt \
 && javac -encoding UTF-8 -nowarn \
      -cp "$(find lib libs -name '*.jar' | tr '\n' ':')" \
      -d build/classes @/tmp/sources.txt

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/classes ./build/classes
COPY --from=build /src/lib ./lib
COPY --from=build /src/libs ./libs
EXPOSE 1135 6667
# config.template is mounted (docker/login.config.properties): the password comes from .env.
CMD ["sh", "-c", "sed \"s/__DB_PASSWORD__/$DB_PASSWORD/g\" config.template > config.properties && exec java -Xmx512m -cp 'build/classes:lib/*:libs/*' org.starloco.locos.kernel.Main"]
