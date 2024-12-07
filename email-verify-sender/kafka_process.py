from kafka import KafkaConsumer
from email_sender import send_email

import json
import logging


def kafka_consume():
    """
    Subcribe to kafka topic, extract information from messages and send it to email sender.
    """
    """
    logger = logging.getLogger("kafka_process")

    topic = "registered-user"
    consumer = KafkaConsumer(
        topic,
        group_id="email-sender",
        bootstrap_servers=["kafka-svc:9092"],
    )
    logger.info(f"Subscribed to {topic}")

    for msg in consumer:
        logger.info("Received message")
        logger.debug(f"Kafka message: {msg}")

        value = msg.value
        data = json.loads(value)
        logger.debug(f"JSON parsed value: {data}")

        try:
            email_data = data["additionalProperties"]["emailDto"]
            logger.debug(f"Email data: {email_data}")

            email = email_data["email"]
            code = email_data["emailVerificationCode"]
            url = email_data["emailVerificationEndpoint"]

            send_email(email, code, url)
        except Exception as e:
            logger.error(f"Failed to parse message", exc_info=e)
    """