FROM node:lts-alpine

WORKDIR /forge-update/

COPY package.json .
COPY yarn.lock .

RUN yarn install --production --silent

COPY . .

ENV DB_IP=localhost
ENV DB_PORT=27017
ENV DB_NAME=updates
ENV PORT=80

ENTRYPOINT []

CMD ["node","index.js"]