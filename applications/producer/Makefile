clean-producer:
	./applications/producer/gradlew clean -p ./applications/producer

test-producer:
	./applications/producer/gradlew test -p ./applications/producer

itest-producer:
	./applications/producer/gradlew integrationTest -x test -p ./applications/producer

build-producer:
	echo "Building Producer App..."
	docker compose -f ./applications/producer/etc/docker/docker-compose.yml build --force-rm
	echo "Producer App Built"
