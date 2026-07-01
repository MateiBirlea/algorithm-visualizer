FROM node:18-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
# npm ci requires lock and package.json in sync; fall back to install to pick up new deps (axios)
RUN npm install
COPY tsconfig.json ./
COPY public public
COPY src src

ARG REACT_APP_AUTH_API
ARG REACT_APP_ALGO_API
ARG REACT_APP_EXPLANATION_API
ARG REACT_APP_CLASSROOM_API
ENV REACT_APP_AUTH_API=${REACT_APP_AUTH_API}
ENV REACT_APP_ALGO_API=${REACT_APP_ALGO_API}
ENV REACT_APP_EXPLANATION_API=${REACT_APP_EXPLANATION_API}
ENV REACT_APP_CLASSROOM_API=${REACT_APP_CLASSROOM_API}

RUN npm run build

FROM nginx:1.25-alpine
WORKDIR /usr/share/nginx/html
COPY --from=build /app/build ./
COPY public/favicon.ico ./favicon.ico
COPY public/manifest.json ./manifest.json

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
