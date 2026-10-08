# The game server: javac over game/src, the jars of game/librerias.
#
# JDK 11: game/librerias holds lombok 1.18.12, which breaks on JDK 16+.
# -proc:none keeps javac from loading it at all (nothing needs it to compile).
FROM eclipse-temurin:11-jdk AS build
WORKDIR /src
COPY game/ ./
RUN mkdir -p build/classes \
 && find src -name '*.java' > /tmp/sources.txt \
 && javac -proc:none -encoding UTF-8 -nowarn \
      -cp "$(find librerias -name '*.jar' | tr '\n' ':')" \
      -d build/classes @/tmp/sources.txt

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /src/build/classes ./build/classes
COPY --from=build /src/librerias ./librerias
# Read from the working directory at run time.
COPY --from=build /src/names.txt ./names.txt
EXPOSE 5555
# config.template is mounted (docker/game.config.properties): the password comes from .env.
CMD ["sh", "-c", "sed \"s/__DB_PASSWORD__/$DB_PASSWORD/g\" config.template > config.properties && exec java -Xmx2g -cp 'build/classes:librerias/*' kernel.Main"]
