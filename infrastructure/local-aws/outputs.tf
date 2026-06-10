# infrastructure/local-aws/outputs.tf

output "s3_bucket_name" {
  description = "The physical S3 bucket name to paste into application.yml"
  value       = aws_s3_bucket.ecommerce_media.bucket
}

output "sqs_queue_url" {
  description = "The physical SQS endpoint URL for event tracking"
  value       = aws_sqs_queue.order_event_queue.id
}