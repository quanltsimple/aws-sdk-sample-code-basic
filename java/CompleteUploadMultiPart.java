package com.example.demo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;

import java.util.ArrayList;
import java.util.List;

public class CompleteUploadMultiPart {
    public static void main(String[] args) {
        // Replace with your AWS information
        String accessKey = "XXX"; // AWS access key
        String secretKey = "XXX"; // AWS secret key
        String bucketName = "XXX"; // The bucket name
        String objectKey = "Archive.zip"; // The key of the object to upload
        String uploadId = "XXX"; // The upload ID of the multipart upload
        List<PartETag> partETags = new ArrayList<>(); // List to hold the part ETags

        // Initialize AWS credentials
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

        // Create an S3 client
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        try {
            // Populate the part ETags list with the ETags of uploaded parts
            partETags.add(new PartETag(1, "XXX"));
            partETags.add(new PartETag(2, "XXX"));
            partETags.add(new PartETag(3, "XXX"));
            partETags.add(new PartETag(4, "XXX"));
            // Add more part ETags as needed for all parts

            // Create a request to complete the multipart upload
            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, partETags);

            // Complete the multipart upload
            s3Client.completeMultipartUpload(completeRequest);

            System.out.println("Multipart upload completed.");
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process it
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client couldn't parse the response from Amazon S3
            e.printStackTrace();
        }
    }
}
