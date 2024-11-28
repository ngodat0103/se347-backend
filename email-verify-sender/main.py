from kafka_process import kafka_consume

from dotenv import load_dotenv

if __name__ == "__main__":
    load_dotenv()
    kafka_consume()