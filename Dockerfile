# Build backend
FROM eclipse-temurin:21.0.1_12-jdk-jammy AS build-back

WORKDIR /app

COPY backend/gradlew .
COPY backend/gradle gradle
COPY backend/build.gradle.kts .
COPY backend/settings.gradle.kts .
COPY backend/src src

RUN ./gradlew clean build -x test

# Build frontend
FROM node:22-slim AS build-front

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable
COPY frontend/. /app
WORKDIR /app
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile
RUN pnpm run build

# Build final image
FROM eclipse-temurin:21.0.1_12-jre-jammy
RUN apt-get update && \
    apt-get install -y curl ca-certificates gnupg && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_20.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update && apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

WORKDIR /app
COPY --from=build-back /app/build/libs/mintstone-backend.jar back/app.jar
COPY --from=build-front /app/build /app/front/
COPY --from=build-front /app/node_modules /app/front/node_modules
COPY --from=build-front /app/package.json /app/front/package.json
EXPOSE 8080
EXPOSE 3000

RUN printf "#!/bin/bash \
    \nset -e \
    \necho 'Starting backend...' \
    \nexec java --enable-preview -jar /app/back/app.jar & \
    \necho 'Starting frontend...' \
    \nexec node /app/front/index.js" > run.sh

RUN chmod +x run.sh
CMD [ "./run.sh" ]
