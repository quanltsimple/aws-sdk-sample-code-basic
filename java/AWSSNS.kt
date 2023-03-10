package com.example.demo

import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest

object AWSSNS {
    @JvmStatic
    fun main(args: Array<String>) {
        var credentials: BasicAWSCredentials? = null
        credentials = try {
            BasicAWSCredentials("", "")
        } catch (ex: Exception) {
            // TODO: handle exception
            throw AmazonClientException("Cannot load the credentials.", ex)
        }

        // Set your Region
        val clientRegion = Regions.AP_NORTHEAST_1
        val snsClient = AmazonSNSClient.builder().withRegion(clientRegion)
            .withCredentials(AWSStaticCredentialsProvider(credentials)).build() as AmazonSNSClient
        val message = "This is your OTP: 12345"
        val phoneNumber = "+84123456789"
        val smsAttributes: MutableMap<String, MessageAttributeValue> = HashMap()
        // Sets the type to Transactional or Promotional.
        smsAttributes["AWS.SNS.SMS.SMSType"] =
            MessageAttributeValue().withStringValue("Transactional").withDataType("String")
        // <set SMS attributes>
        sendSMSMessage(snsClient, message, phoneNumber, smsAttributes)
    }

    fun sendSMSMessage(
        snsClient: AmazonSNSClient, message: String?, phoneNumber: String?,
        smsAttributes: Map<String, MessageAttributeValue>?
    ) {
        val result = snsClient.publish(
            PublishRequest().withMessage(message).withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes)
        )
        println(result) // Prints the message ID.
    }
}
