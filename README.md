Different structured log examples
---------------------------------

# Overview 
This repository contains a number of examples to demonstrate how to structure logs in a way that makes it easy to 
search and filter them. The examples are written in Java and use different logging libraries.

The examples are plain Java applications but the same principles can be applied to any language, frameworks and 
logging library. 

The examples are:

- log4j2_json_example - using Java with log4j2 and the output is in JSON format to stdout
- log4j2_object_example - using Java with log4j2 and the output is OTLP protocol for logs directly to a OTLP receiver

> All examples have been tested with Openjdk 19 but should work with any version of Java 11 or later.

The log records for all examples are ingest into Grafana Loki using the Grafana Agent.
For simplicity, sign up for a free account at [Grafana Cloud](https://grafana.com/products/cloud/). 
Create an API key and use the key to configure the Grafana Agent with your api key.

# Setup and run Grafana Agent

Update the file `grafana-agent-flow.river` file with your url and API key that you can find in your free Grafana Cloud 
account. 

```shell
cd grafana_agent
# Download the Grafana Agent from https://github.com/grafana/agent/releases, below is for linux
wget https://github.com/grafana/agent/releases/download/v0.39.2/grafana-agent-linux-amd64.zip
unzip grafana-agent-linux-amd64.zip
export AGENT_MODE=flow 

# Run the Grafana Agent
./grafana-agent-linux-amd64 run ./grafana-agent-flow.river
```
Connect to the agent in a browser at `http://localhost:12345`

# Examples

## log4j2_json_example
To run the example, use the following command:

```shell
cd log4j2_json_example
mvn clean compile 
mvn exec:java | grep -ve "^\[" > /tmp/log4j2_json_example.log 
```

## log4j2_otel_example

To run the example, use the following command:

```shell
cd log4j2_otel_example
mvn clean compile
mvn exec:java 
```
The output is sent to the Grafana agent OTLP receiver running on `localhost:4317`

# Inspect logs in Grafana Loki
The logs are sent to Grafana Loki and can be inspected in the Explore view in Grafana. 
To view the logs from the example "log4j2_json_example", use the following query:
```shell
{exporter="log4j2json"} | json
```
To view the logs from the example "log4j2_otel_example", use the following query:
```shell
{exporter="OTLP"} | json
```
