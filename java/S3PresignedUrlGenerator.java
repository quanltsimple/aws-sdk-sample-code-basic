package com.example.demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;

import java.net.URL;
import java.util.Date;

public class S3PresignedUrlGenerator {

    public static void main(String[] args) {
        // Replace with your AWS information
        String accessKey = "XXX"; // AWS access key
        String secretKey = "XXX"; // AWS secret key
        String bucketName = "XXX"; // The bucket name
        String objectKey = "Archive.zip"; // The key of the object to upload
        int totalParts = 4; // Total number of parts for multipart upload
        int expirationSeconds = 3600; // Expiration time for the presigned URLs (1 hour)

        // Initialize AWS credentials
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        // Create an S3 client
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.AP_SOUTHEAST_1) // Specify the AWS region
                .build();

        try {
            // Start the multipart upload and get the upload ID
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, objectKey);
            String uploadId = s3Client.initiateMultipartUpload(initRequest).getUploadId();
            System.out.println("uploadId : " + uploadId);

            // Generate presigned URLs for each part
            for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
                GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.PUT) // Use PUT method for upload
                        .withExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000)); // Set expiration time for URL
                generatePresignedUrlRequest.addRequestParameter("uploadId", uploadId); // Add upload ID as a request parameter
                generatePresignedUrlRequest.addRequestParameter("partNumber", Integer.toString(partNumber)); // Add part number as a request parameter
                URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest); // Generate presigned URL
                System.out.println("Presigned URL for part " + partNumber + ": " + url);
            }
        } catch (AmazonServiceException e) {
            // Catch Amazon service exceptions
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Catch SDK client exceptions
            e.printStackTrace();
        }
    }

}
