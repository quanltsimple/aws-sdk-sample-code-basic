package com.example.demo;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClientBuilder;
import com.amazonaws.services.certificatemanager.model.DescribeCertificateRequest;
import com.amazonaws.services.certificatemanager.model.DescribeCertificateResult;
import com.amazonaws.services.certificatemanager.model.ListCertificatesRequest;
import com.amazonaws.services.certificatemanager.model.ListCertificatesResult;
import com.amazonaws.services.certificatemanager.model.RequestCertificateRequest;
import com.amazonaws.services.certificatemanager.model.RequestCertificateResult;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.GetHostedZoneRequest;
import com.amazonaws.services.route53.model.GetHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;

public class AWSACM {

    public static void main(String[] args) {
        BasicAWSCredentials credentials = null;

        try {
            credentials = new BasicAWSCredentials("", "");
        } catch (Exception ex) {
            // TODO: handle exception
            throw new AmazonClientException("Cannot load the credentials", ex);
        }

        // Your domain request certificate
        String domainRequest = "*.example.com";

        // Set your Region
        Regions clientRegion = Regions.AP_NORTHEAST_2;

        // Create ACM client
        AWSCertificateManager acmClient = AWSCertificateManagerClientBuilder.standard().withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        // Create Route53 Client
        AmazonRoute53 clientRoute53 = AmazonRoute53ClientBuilder.standard().withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        // Get list certificate created on ACM

        // Create a request object and set the parameters.
        ListCertificatesRequest req = new ListCertificatesRequest();
        // List status of certificate your want query
        List<String> Statuses = Arrays.asList("ISSUED", "EXPIRED", "PENDING_VALIDATION", "FAILED");
        req.setCertificateStatuses(Statuses);
        // Set max value you want query 1 -> 100xxx
        req.setMaxItems(10);

        // Retrieve the list of certificates.
        ListCertificatesResult listCertificatesResult = null;

        try {
            listCertificatesResult = acmClient.listCertificates(req);
            // Display the certificate list.
            System.out.println(listCertificatesResult.getCertificateSummaryList());
        } catch (Exception ex) {
            // TODO: handle exception
            throw ex;
        }

        // Create a request object and set the input parameters.
        RequestCertificateRequest requestCertificateRequest = new RequestCertificateRequest();
        requestCertificateRequest.setDomainName(domainRequest);
        requestCertificateRequest.setValidationMethod("DNS");

        // Create a result object and display the certificate ARN.
        RequestCertificateResult requestCertificateResult = null;

        try {

            requestCertificateResult = acmClient.requestCertificate(requestCertificateRequest);
            var certificateArnCreated = requestCertificateResult.getCertificateArn();
            System.out.print("CertificateArn Created: " + certificateArnCreated);

            // Add the following CNAME record to the DNS configuration for your domain
            // NOTE: If you host your domain in Route53

            DescribeCertificateRequest describeCertificateRequest = new DescribeCertificateRequest();
            describeCertificateRequest.setCertificateArn(certificateArnCreated);
            DescribeCertificateResult describeCertificateResult = acmClient
                    .describeCertificate(describeCertificateRequest);

            System.out.println("Describe Certificate Result: " + describeCertificateResult);

            var resourceRecord = describeCertificateResult.getCertificate().getDomainValidationOptions().get(0)
                    .getResourceRecord();
            GetHostedZoneResult getHostedZoneResult = clientRoute53
                    .getHostedZone(new GetHostedZoneRequest("YOUR-HOSTED-ZONE-ID"));
            HostedZone zone = getHostedZoneResult.getHostedZone();

            ResourceRecordSet recordSet = new ResourceRecordSet().withName(resourceRecord.getName())
                    .withType(RRType.CNAME).withTTL(300L)
                    .withResourceRecords(new ResourceRecord().withValue(resourceRecord.getValue()));

            ChangeResourceRecordSetsRequest reqChange = new ChangeResourceRecordSetsRequest()
                    .withHostedZoneId(zone.getId()).withChangeBatch(new ChangeBatch().withChanges(
                            new Change().withAction(ChangeAction.CREATE).withResourceRecordSet(recordSet)));

            ChangeResourceRecordSetsResult changeResourceRecordSetsResult = null;

            changeResourceRecordSetsResult = clientRoute53.changeResourceRecordSets(reqChange);

            System.out.print(changeResourceRecordSetsResult);

        } catch (Exception e) {
            // TODO: handle exception
            throw e;
        }
    }

}
