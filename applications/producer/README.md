# PRODUCER

## Description
A **Kotlin application using Kafka Producers with Spring Boot** is like having a network of news agencies and broadcasters in our metaphorical city. These producers are responsible for creating and sending out news (messages) to the city's inhabitants (Kafka topics) efficiently and reliably.

### How It Works

* `Creating Messages`: Just as a news agency gathers information to create a news story, Kafka Producers in your application prepare data messages. This could involve collecting data from user interactions, system events, or any other source of data within your application.
* `Sending Messages`: Once the news story (message) is ready, it's broadcasted to the city (sent to a Kafka topic). In a Spring Boot application, this is made simple with the KafkaTemplate class. You can easily send messages to any topic with minimal configuration, thanks to Spring Boot's auto-configuration capabilities.
* `Ensuring Delivery`: Just as a broadcaster would want to ensure their message is received, Kafka Producers can configure message delivery semantics (like acks, retries) to balance between reliability and performance. Spring Boot makes configuring these producer settings straightforward through application properties.

## Use Cases

* `Event Sourcing`: Imagine every action in the city being recorded for future reference. Kafka Producers allow applications to capture and store state-changing events in a durable and immutable manner, enabling complex systems to rebuild state from a series of events.
* `Data Integration`: Just as various city departments might send information to each other, Kafka Producers enable different systems to share data seamlessly, whether it's for real-time analytics, feeding data lakes, or simply synchronizing state across services.
* `Notifications and Alerts`: Broadcasters alerting the city to breaking news is akin to applications sending out notifications or alerts based on certain triggers or conditions, ensuring that other parts of the system or end-users are informed in real time.

## Current example

The producer application is just a scheduled job running every 5 seconds (after a delay of 30 seconds, to prevent racing conditions on the topics creation), which sends random names associated to a user id.

In fact, it is using the same Ids used by the stream so this is a good starting point to start building on [your own product](../../PROPOSAL.md).
