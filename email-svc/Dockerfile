FROM python:3.12 AS builder
RUN apt-get update && apt-get install -y build-essential
WORKDIR /app
COPY requirements.txt .
RUN --mount=type=cache,target=/root/.cache/pip pip install -r requirements.txt

FROM python:3.12
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app

COPY --from=builder /usr/local /usr/local
COPY . .

RUN chmod 500 ./main.py && chown -R appuser:appgroup .
USER appuser
CMD ["python", "./main.py"]
