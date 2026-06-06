# main.tf

# 1. Pull down the official AWS Provider Translator Plugin
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# 2. Reroute the provider to point AWAY from the cloud and loop into LocalStack
provider "aws" {
  access_key                  = "mock_access_key"
  secret_key                  = "mock_secret_key"
  region                      = "us-east-1"
  
  # Tell Terraform to skip checking real AWS platform configurations
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  # CRITICAL: Force all AWS API calls to hit your local machine's Docker network container
  endpoints {
    s3  = "http://localhost:4566"
    sqs = "http://localhost:4566"
  }
}

# 3. Create a simulated S3 Bucket for your e-commerce platform
resource "aws_s3_bucket" "ecommerce_media" {
  bucket = "lnreddydev-ecommerce-media-local"

  tags = {
    Environment = "Local-Dev"
    Platform    = "Spring-Microservices"
  }
}

# 4. Create a simulated SQS Queue for message event flows
resource "aws_sqs_queue" "order_event_queue" {
  name                      = "order-events-queue-local"
  message_retention_seconds = 86400 # Keep messages active for 1 day
}
