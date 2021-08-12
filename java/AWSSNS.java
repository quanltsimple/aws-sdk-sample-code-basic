package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

public class AWSSNS {
    public static void main(String[] args) {
        BasicAWSCredentials credentials = null;

        try {
            credentials = new BasicAWSCredentials("", "");
        } catch (Exception ex) {
            // TODO: handle exception
            throw new AmazonClientException("Cannot load the credentials.", ex);
        }

        // Set your Region
        Regions clientRegion = Regions.AP_NORTHEAST_2;

        AmazonSNSClient snsClient = (AmazonSNSClient) AmazonSNSClient.builder().withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        String message = "This is your OTP: 12345";
        String phoneNumber = "+0123456789";
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
        // Sets the type to Transactional or Promotional.
        smsAttributes.put("AWS.SNS.SMS.SMSType",
                new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));
        // <set SMS attributes>
        sendSMSMessage(snsClient, message, phoneNumber, smsAttributes);
    }

    public static void sendSMSMessage(AmazonSNSClient snsClient, String message, String phoneNumber,
            Map<String, MessageAttributeValue> smsAttributes) {
        PublishResult result = snsClient.publish(new PublishRequest().withMessage(message).withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes));
        System.out.println(result); // Prints the message ID.
    }

}
