FROM node:18-alpine AS frontend-builder

COPY frontend/package.json .
COPY frontend/yarn.lock .

RUN yarn install --non-interactive

COPY frontend .

RUN yarn build

FROM openjdk:17-jdk-alpine

COPY --from=frontend-builder dist /var/www/
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew backend:build

ENTRYPOINT ["java","-jar","backend/build/libs/app.jar"]
