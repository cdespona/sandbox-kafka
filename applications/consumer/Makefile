clean-consumer:
	./applications/consumer/gradlew clean -p ./applications/consumer

test-consumer:
	./applications/consumer/gradlew test -p ./applications/consumer

itest-consumer:
	./applications/consumer/gradlew integrationTest -x test -p ./applications/consumer

build-consumer:
	echo "Building Consumer App..."
	docker compose -f ./applications/consumer/etc/docker/docker-compose.yml build --force-rm
	echo "Consumer App Built"

