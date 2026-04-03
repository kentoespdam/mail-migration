IMAGE_NAME := smartoffice-mail
IMAGE_TAG  := latest

.PHONY: jar image build run clean

## Build JAR inside Docker (no local JDK needed)
jar:
	docker compose -f docker/compose.yml build builder
	UID=$$(id -u) GID=$$(id -g) docker compose -f docker/compose.yml run --rm builder

## Build runtime image (requires JAR in build/libs/)
image:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) -f docker/Dockerfile .

## Build JAR + image
build: jar image

## Start full stack (app + db + redis)
run:
	docker compose -f docker/compose.yml up -d mail-be mariadb redis

## Stop and clean up
clean:
	docker compose -f docker/compose.yml down -v
	rm -rf build/libs/*.jar
