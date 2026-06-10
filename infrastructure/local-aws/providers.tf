# infrastructure/local-aws/providers.tf

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

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