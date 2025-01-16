-include .env
-include ./applications/stream/Makefile
-include ./applications/producer/Makefile
-include ./applications/consumer/Makefile
export

# Get the absolute path to the running Makefile
ROOT_DIR := $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))

# Colours
BLUE:=			\033[0;34m
RED:=			\033[0;31m
LIGHT_RED:=		\033[1;31m
WHITE:=			\033[1;37m
LIGHT_VIOLET := \033[1;35m
NO_COLOUR := 	\033[0m

# Environment : { dev, staging, prod }
ENV := dev

PROJECT_NAME := sandbox-kafka
PROJECT_PORT := 8080

DOCKER_IMAGE_NAME := sbx-kafka

MSG_SEPARATOR := "*********************************************************************"
MSG_IDENT := "    "


.SILENT:
help:
	echo "\n${MSG_SEPARATOR}\n$(LIGHT_VIOLET)$(PROJECT_NAME)$(NO_COLOUR)\n${MSG_SEPARATOR}\n"
	echo "${MSG_IDENT}=======   ✨  BASIC   =====================================\n   "
	echo "${MSG_IDENT}  ⚠️   Requirements : Java 21 \n"
	echo "${MSG_IDENT}  clean                   -  🚮  Erase the 📁 build/"
	echo "${MSG_IDENT}  test                    -  ✅  Run Unit tests only"
	echo "${MSG_IDENT}  itest                   -  ✅  Run Integration tests only"
	echo "${MSG_IDENT}=======   🐳  DOCKER   =====================================\n"
	echo "${MSG_IDENT}  ℹ️   To work with $(PROJECT_NAME) running alone in a container"
	echo "${MSG_IDENT}  ⚠️   Requirements : docker \n"
	echo "${MSG_IDENT}  dk-build                -  📦  Build a docker image with the .jar"
	echo "${MSG_IDENT}  up                      -  🚀  Start container ${DOCKER_IMAGE_NAME}"
	echo "${MSG_IDENT}  down                    -  🛑  Stop container ${DOCKER_IMAGE_NAME}"
	echo "${MSG_IDENT}  restart                 -  ♻️  Rebuild the application and launch app"
	echo "${MSG_IDENT}  dk-logs                 -  📃️  See logs from the running container ${DOCKER_IMAGE_NAME}"
	echo "${MSG_IDENT}  dk-shell                -  💻️  Shell in the running container ${DOCKER_IMAGE_NAME}"
	echo "${MSG_IDENT}  dk-rmi                  -  🧹  Removing image with name ${DOCKER_IMAGE_NAME}"
	echo "${MSG_IDENT}=======   🧵  KAFKA    =====================================\n"
	echo "${MSG_IDENT}  gen_data_connect        -  💽  Switching on connectors to generate data automatically"
	echo

######################################################################
########################   BASIC    #################################
######################################################################

clean: clean-stream clean-consumer clean-producer
	echo "Clean Complete"

test: test-stream test-consumer test-producer
	echo "Test Complete"

itest: itest-stream itest-consumer itest-producer
	echo "Integration Test Complete"

######################################################################
########################   🐳 DOCKER    ##############################
######################################################################
dk-build: dk-rmi env-variables
	echo "Building Kafka Playground..."
	docker compose build --force-rm
	echo "Kafka Playground Built"

up: env-variables dk-build
	echo "\n\n${MSG_SEPARATOR}\n\n 🐳 up => 🚀  Start container ${DOCKER_IMAGE_NAME} \n\n${MSG_SEPARATOR}\n\n"

	docker compose up -d

	echo "\n\n${MSG_SEPARATOR}\n\n  🐳 Your apps are running 🚀\n"
	echo "  To open the Confluent Control Center -> http://localhost:9021"
	echo "\n${MSG_SEPARATOR}\n\n"

down:
	echo "\n\n${MSG_SEPARATOR}\n\n 🐳 down => 🚀  Stop container ${DOCKER_IMAGE_NAME} \n\n${MSG_SEPARATOR}\n\n"

	-docker compose down --remove-orphans

restart: env-variables down dk-build up

dk-logs:
	echo "\n\n${MSG_SEPARATOR}\n\n 🐳 dk-logs => 📃️  See logs from the running container ${DOCKER_IMAGE_NAME} \n\n${MSG_SEPARATOR}\n\n"

	-docker compose logs kfk-consumer kfk-producer kfk-stream


dk-shell:
	echo "\n\n${MSG_SEPARATOR}\n\n 🐳 dk-logs => 💻️  Shell in the running container ${DOCKER_IMAGE_NAME} \n\n${MSG_SEPARATOR}\n\n"

	docker compose exec ${PROJECT_NAME} sh


dk-rmi:
	echo "\n\n${MSG_SEPARATOR}\n\n 🐳 dk-rmi => 🧹  Removing the image ${DOCKER_IMAGE_NAME}\n\n${MSG_SEPARATOR}\n\n"

	-docker rmi ${DOCKER_IMAGE_NAME}

#################KAFKA######################
gen_data_connect:
	@docker compose exec broker curl -L -O -H 'Accept: application/vnd.github.v3.raw' https://raw.githubusercontent.com/confluentinc/kafka-connect-datagen/master/config/connector_pageviews_cos.config
	@docker compose exec broker curl -L -O -H 'Accept: application/vnd.github.v3.raw' https://raw.githubusercontent.com/confluentinc/kafka-connect-datagen/master/config/connector_users_cos.config
	@docker compose exec broker curl -X POST connect:8083/connectors -H "Content-Type: application/json" --data @connector_pageviews_cos.config
	@docker compose exec broker curl -X POST connect:8083/connectors -H "Content-Type: application/json" --data @connector_users_cos.config


######################################################################
###########################   OTHERS    ##############################
######################################################################

env-variables:
	if [ -f .env ]; then \
		true ; \
    else \
	    cp .env-sample .env ; \
		echo "${LIGHT_RED}ERROR - File .env not found:${NO_COLOUR} Generated the file .env, please ${LIGHT_VIOLET}relaunch the last command${NO_COLOUR}"; \
		false ; \
	fi
