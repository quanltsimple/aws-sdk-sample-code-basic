package com.example.demo;

import java.util.Collections;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingResult;
import com.amazonaws.services.apigateway.model.CreateDomainNameRequest;
import com.amazonaws.services.apigateway.model.CreateDomainNameResult;
import com.amazonaws.services.apigateway.model.EndpointConfiguration;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameResult;
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

public class AWSAPIGW {

    public static void main(String[] args) {

        BasicAWSCredentials credentials = null;

        try {
            credentials = new BasicAWSCredentials("", "");
        } catch (Exception ex) {
            // TODO: handle exception
            throw new AmazonClientException("Cannot load the credentials", ex);
        }

        // Your domain request
        String domainRequest = "*.example.com";

        // Your Domain CertificateArn from AWS ACM
        // If you are going to create a Domain with End point type is EDGE,
        // your Certificate must be created in Region US East (N. Virginia) us-east-1
        String certificateArn = "arn:aws:acm:ap-northeast-1:xxxxx:certificate/xxxx-xxx-xxxx-xxxx-xxxx";

        // Set your Region
        Regions clientRegion = Regions.AP_NORTHEAST_2;

        // Create API Gateway Client
        AmazonApiGateway clientAPIGateway = AmazonApiGatewayClientBuilder.standard().withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        EndpointConfiguration endpointConfiguration = new EndpointConfiguration();

        // Set End point Types of Domain: REGIONAL or EDGE (use with CloudFront AWS
        // Managed)
        List<String> Types = Collections.singletonList("REGIONAL");
        endpointConfiguration.setTypes(Types);

        CreateDomainNameRequest request = new CreateDomainNameRequest();
        request.setEndpointConfiguration(endpointConfiguration);
        request.setDomainName(domainRequest);

        // request.setCertificateArn(certificateArn); //If use End point type is EDGE
        request.setRegionalCertificateArn(certificateArn); // If use End point type is REGIONAL

        // Create a result object and display the certificate ARN.
        CreateDomainNameResult createDomainNameResult = null;

        try {
            createDomainNameResult = clientAPIGateway.createDomainName(request);
            // Display the Result
            System.out.println(createDomainNameResult);
        } catch (Exception ex) {
            // TODO: handle exception
            throw ex;
        }

        // Mapping domain with API Gateway Rest API
        CreateBasePathMappingRequest createBasePathMappingRequest = new CreateBasePathMappingRequest();
        createBasePathMappingRequest.setDomainName(domainRequest);
        createBasePathMappingRequest.setRestApiId("Rest-API-Id");
        createBasePathMappingRequest.setStage("Your-Stage");

        CreateBasePathMappingResult createBasePathMappingResult = null;

        try {
            createBasePathMappingResult = clientAPIGateway.createBasePathMapping(createBasePathMappingRequest);
            // Display the createBasePathMappingResult
            System.out.println(createBasePathMappingResult);
        } catch (Exception ex) {
            // TODO: handle exception
            throw ex;
        }

        // Get all info of your domain created on API Gateway
        GetDomainNameRequest getDomainNameRequest = new GetDomainNameRequest();
        getDomainNameRequest.setDomainName(domainRequest);

        GetDomainNameResult getDomainNameResult = clientAPIGateway.getDomainName(getDomainNameRequest);
        // Display the info of your domain created on API Gateway
        System.out.println(getDomainNameResult);

        // NOTE: If you host your domain in Route53
        // Create Record of your domain & mapping to your domain in API Gateway

        // Create Route53 Client
        AmazonRoute53 clientRoute53 = AmazonRoute53ClientBuilder.standard().withRegion(clientRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        var regionalDomainName = getDomainNameResult.getRegionalDomainName();

        GetHostedZoneResult result = clientRoute53.getHostedZone(new GetHostedZoneRequest("YOUR-HOSTED-ZONE-ID"));
        HostedZone zone = result.getHostedZone();

        ResourceRecordSet recordSet = new ResourceRecordSet().withName(domainRequest).withType(RRType.CNAME)
                .withTTL(300L).withResourceRecords(new ResourceRecord().withValue(regionalDomainName));

        ChangeResourceRecordSetsRequest reqChange = new ChangeResourceRecordSetsRequest().withHostedZoneId(zone.getId())
                .withChangeBatch(new ChangeBatch()
                        .withChanges(new Change().withAction(ChangeAction.CREATE).withResourceRecordSet(recordSet)));

        ChangeResourceRecordSetsResult changeResourceRecordSetsResult = null;

        try {
            changeResourceRecordSetsResult = clientRoute53.changeResourceRecordSets(reqChange);
            // Display the changeResourceRecordSetsResult
            System.out.println(changeResourceRecordSetsResult);
        } catch (Exception ex) {
            // TODO: handle exception
            throw ex;
        }

    }

}
