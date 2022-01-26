package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.ListDomainsResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<VoiceIdClient> proxyClient,
        final Logger logger) {

        final List<ResourceModel> models;

        final String nextToken;

        try {
            final ListDomainsResponse
                awsResponse =
                proxyClient.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()),
                                                         proxyClient.client()::listDomains);
            models = Translator.translateFromListRequest(awsResponse);
            nextToken = awsResponse.nextToken();
            logger.log(String.format("%s has successfully been listed.", ResourceModel.TYPE_NAME));
        } catch (final AwsServiceException e) {
            throw Translator.translateToCfnException(e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .nextToken(nextToken)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
