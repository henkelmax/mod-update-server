FROM node:12-alpine AS frontend-builder

COPY frontend/package.json .
COPY frontend/yarn.lock .

RUN yarn install --non-interactive

COPY frontend .

RUN yarn build


FROM node:12-alpine

WORKDIR /forge-update

COPY package.json .
COPY yarn.lock .

RUN yarn install --production --silent

COPY *.js .
COPY --from=frontend-builder dist frontend/dist

ENV DB_IP=localhost
ENV DB_PORT=27017
ENV DB_NAME=updates
ENV PORT=80

ENTRYPOINT []

CMD ["node","index.js"]