FROM node:slim AS base

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable
COPY . /app/front
WORKDIR /app/front

FROM base AS build-front
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile
RUN pnpm run build

FROM base
COPY --from=build-front /app/front/node_modules /app/front/node_modules
COPY --from=build-front /app/front/build /app/front/build
EXPOSE 3000
CMD [ "node", "build/index.js" ]
