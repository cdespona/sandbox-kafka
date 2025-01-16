clean-stream:
	./applications/stream/gradlew clean -p ./applications/stream

test-stream:
	./applications/stream/gradlew test -p ./applications/stream

itest-stream:
	./applications/stream/gradlew integrationTest -x test -p ./applications/stream

build-stream:
	echo "Building Stream App..."
	docker compose -f ./applications/stream/etc/docker/docker-compose.yml build --force-rm
	echo "Stream App Built"
