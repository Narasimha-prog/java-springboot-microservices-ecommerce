# infrastructure/local-aws/main.tf

# 1. Create a simulated S3 Bucket for your e-commerce platform media
resource "aws_s3_bucket" "ecommerce_media" {
  bucket = "lnreddydev-ecommerce-media-local"

  tags = {
    Environment = "Local-Dev"
    Platform    = "Spring-Microservices"
  }
}

# 2. Create a simulated SQS Queue for microservice message event flows
resource "aws_sqs_queue" "order_event_queue" {
  name                      = "order-events-queue-local"
  message_retention_seconds = 86400 # Keep messages active for 1 day
}