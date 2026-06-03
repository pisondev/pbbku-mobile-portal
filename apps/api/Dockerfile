FROM golang:1.23-bookworm AS build

WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=1 GOOS=linux go build -o /out/pbbku-api ./cmd/server

FROM debian:bookworm-slim

WORKDIR /app
RUN apt-get update \
	&& apt-get install -y --no-install-recommends ca-certificates \
	&& rm -rf /var/lib/apt/lists/*

COPY --from=build /out/pbbku-api /usr/local/bin/pbbku-api
COPY migrations ./migrations
COPY seeds ./seeds

EXPOSE 8080
CMD ["pbbku-api", "serve", "-addr", ":8080"]
