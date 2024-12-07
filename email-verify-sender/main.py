import logging
from kafka_process import kafka_consume
from dotenv import load_dotenv

# Cấu hình logging
logging.basicConfig(
    level=logging.DEBUG,  # Cấp độ log (DEBUG để hiển thị tất cả các log)
    format='%(asctime)s - %(levelname)s - %(message)s',  # Định dạng log
)

logger = logging.getLogger(__name__)  # Tạo logger

if __name__ == "__main__":
    try:
        logger.info("Loading environment variables from .env file.")
        load_dotenv()
        logger.info("Run succesfully .env")

        logger.info("Starting Kafka consumer.")
        kafka_consume()  # Hàm kafka_consume sẽ xử lý việc tiêu thụ dữ liệu từ Kafka
        logger.info("Run succesfully consumer")
        
    except Exception as e:
        logger.error(f"An error occurred: {e}")
