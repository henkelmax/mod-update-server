# Example Docker Compose Configuration

``` yml
version: "3.9"

services:
  update-server:
    build: "https://github.com/henkelmax/mod-update-server.git#master"
    restart: "always"
    container_name: "update-server"
    environment:
      MASTER_KEY: "b13c2739-f6e4-4123-b486-8f44c3f53bac" # Change this to a secret value
      DB_IP: "update-mongodb"
    networks:
      - "web"
      - "update-server"
    depends_on:
      - "update-mongodb"
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.updateHttp.rule=Host(`update.maxhenkel.de`, `www.update.maxhenkel.de`)"
      - "traefik.http.routers.updateHttp.entrypoints=web"
      - "traefik.http.routers.updateHttp.middlewares=httpsRedirect@file"
      - "traefik.http.routers.updateHttps.rule=Host(`update.maxhenkel.de`, `www.update.maxhenkel.de`)"
      - "traefik.http.routers.updateHttps.entrypoints=web-secure"
      - "traefik.http.routers.updateHttps.tls.certresolver=certHttp"
      - "traefik.http.routers.updateHttps.tls=true"
      - "traefik.http.services.update_service.loadbalancer.server.port=8088"
  update-mongodb:
    image: "mongo:6"
    restart: "always"
    container_name: "update-mongodb"
    volumes:
      - "/dockerdata/update-server/mongo:/data/db"
    networks:
      - "update-server"
  update-prometheus:
    image: "bitnami/prometheus:2.48.0"
    restart: "always"
    container_name: "update-prometheus"
    volumes:
      - "/etc/timezone:/etc/timezone:ro"
      - "/etc/localtime:/etc/localtime:ro"
      - "/dockerdata/update-server/prometheus/prometheus.yml:/opt/bitnami/prometheus/conf/prometheus.yml"
      - "/dockerdata/update-server/prometheus/data:/opt/bitnami/prometheus/data"
    networks:
      - "update-server"
  update-grafana:
    image: "grafana/grafana-oss:10.2.2"
    restart: "always"
    container_name: "update-grafana"
    volumes:
      - "/etc/timezone:/etc/timezone:ro"
      - "/etc/localtime:/etc/localtime:ro"
      - "/dockerdata/update-server/grafana:/var/lib/grafana"
    networks:
      - "web"
      - "update-server"
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.grafanaHttp.rule=Host(`grafana.update.maxhenkel.de`, `www.grafana.update.maxhenkel.de`)"
      - "traefik.http.routers.grafanaHttp.entrypoints=web"
      - "traefik.http.routers.grafanaHttp.middlewares=httpsRedirect@file"
      - "traefik.http.routers.grafanaHttps.rule=Host(`grafana.update.maxhenkel.de`, `www.grafana.update.maxhenkel.de`)"
      - "traefik.http.routers.grafanaHttps.entrypoints=web-secure"
      - "traefik.http.routers.grafanaHttps.tls.certresolver=certHttp"
      - "traefik.http.routers.grafanaHttps.tls=true"
      - "traefik.http.services.grafana_service.loadbalancer.server.port=3000"
networks:
  web:
    external: true
  update-server:
    internal: true

```
