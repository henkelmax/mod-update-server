FROM node:alpine

WORKDIR /forge-update/

COPY package.json .
COPY yarn.lock .

RUN yarn install --production --silent

COPY . .

ENTRYPOINT []

CMD ["node","index.js"]