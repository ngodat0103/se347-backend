FROM maven:3.9.8-eclipse-temurin-21 AS app-builder
WORKDIR /build
COPY --chmod=500 mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN --mount=type=cache,sharing=shared,target=~/.m2/repository/ \
    ./mvnw dependency:resolve-plugins dependency:resolve
COPY ./src/main ./src/main
ARG VERSION
ARG SERVICE_NAME
ENV JAR_PATH=/build/target/${SERVICE_NAME}-${VERSION}.jar
RUN  ./mvnw clean package -q -DskipTests -Drevision=${VERSION} -Dservice_name=${SERVICE_NAME} && \
    java -Djarmode=tools -jar "$JAR_PATH" extract --layers --launcher --destination /build/target/layers

FROM eclipse-temurin:21-jdk-alpine AS jre-builder
RUN  jlink --verbose --add-modules ALL-MODULE-PATH --strip-debug --no-man-pages --no-header-files \
     --compress=2 --output /jre

FROM alpine:3.20.3@sha256:beefdbd8a1da6d2915566fde36db9db0b524eb737fc57cd1367effd16dc0d06d
RUN apk add --no-cache curl && \
    adduser -D -s /bin/bash app
ENV JAVA_HOME=/jre
ENV PATH="$JAVA_HOME/bin:$PATH"
COPY --from=jre-builder /jre $JAVA_HOME
ARG SERVICE_PORT
WORKDIR /opt/app

ENV LAYER_PATH=/build/target/layers
COPY --from=app-builder --chown=app:app --chmod=500 $LAYER_PATH/dependencies/ ./
COPY --from=app-builder --chown=app:app --chmod=500 $LAYER_PATH/spring-boot-loader/ ./
COPY --from=app-builder --chown=app:app --chmod=500 $LAYER_PATH/snapshot-dependencies ./
COPY --from=app-builder --chown=app:app --chmod=500  $LAYER_PATH/application ./
EXPOSE ${SERVICE_PORT}
ARG SERVICE_NAME
LABEL org.opencontainers.image.source=https://github.com/ngodat0103/se347-backend.git
LABEL  org.opencontainers.image.description="The ${SERVICE_NAME}"
LABEL  org.opencontainers.image.title=${SERVICE_NAME}
USER app
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]