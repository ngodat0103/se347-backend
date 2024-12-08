from kafka_process import kafka_consume

from dotenv import load_dotenv

import logging

if __name__ == "__main__":
    load_dotenv()
    
    logging.basicConfig()
    logging.getLogger("kafka_process").setLevel(logging.INFO)
    logging.getLogger("email_sender").setLevel(logging.INFO)

    kafka_consume()