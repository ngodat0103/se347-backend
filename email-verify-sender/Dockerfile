# FROM python:3.12-alpine
# WORKDIR /usr/src/app

# COPY requirements.txt ./
# RUN pip install --no-cache-dir -r requirements.txt

# COPY . .

# CMD [ "python", "./main.py" ]

# Sử dụng Python với Alpine Linux (nhẹ và bảo mật hơn)
FROM python:3.12-alpine

# Bước 1: Cài đặt các gói cần thiết
RUN apk add --no-cache \
    gcc \
    musl-dev \
    libffi-dev \
    openssl-dev \
    python3-dev \
    build-base \
    && addgroup -S appgroup && adduser -S appuser -G appgroup

# Bước 2: Đặt thư mục làm việc và thiết lập quyền sở hữu
WORKDIR /usr/src/app
RUN chown appuser:appgroup /usr/src/app

# Bước 3: Copy file requirements.txt trước để tận dụng cache
COPY requirements.txt .

# Bước 4: Cài đặt thư viện Python với cache pip
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install --no-cache-dir -r requirements.txt

# Bước 5: Copy toàn bộ mã nguồn và thiết lập quyền
COPY . .
RUN chmod=500 ./main.py && chown -R appuser:appgroup .

# Bước 6: Chạy ứng dụng với người dùng không phải root
USER appuser

# Bước 7: Thiết lập lệnh khởi chạy ứng dụng
CMD ["python", "./main.py"]
