# AWS::VoiceID::Domain

This package has two main components:

1. The JSON schema describing a VoiceID Domain resource, `aws-voiceid-domain.json`
1. The CRUDL resource handler implementations and unit tests.

The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

## Testing
### Prerequisites
The CloudFormation CLI is required for development and testing. Installation instructions can be found here -
https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/what-is-cloudformation-cli.html
### Unit tests
This project's unit tests are a suite of JUnit tests located in the `src/test` directory and can be run using whatever method you prefer.
### SAM Tests
To test changes to the handlers without needing to deploy them to Lambda, you can run them locally using [SAM](https://aws.amazon.com/serverless/sam/).

To do so, first create a new file in the `sam-tests` directory. This file should contain JSON like the following:

```ts
{
    "credentials": {
        // Real STS credentials need to go here
        "accessKeyId": string,
        "secretAccessKey": string,
        "sessionToken": string
    },
    "action": "CREATE" | "READ" | "UPDATE" | "DELETE" | "LIST",
    "request": {
        "clientRequestToken": string, // Can be any UUID
        "desiredResourceState": ResourceModel,
        "logicalResourceIdentifier": string
    },
    "callbackContext": CallbackContext | null
}
```

Before you run a test, you'll need to make sure that the credentials in the test file are active and valid.

To run the test, you can invoke the handler on the command line using

```
sam local invoke TestEntrypoint --event sam-tests/<filename>
```
### Contract Tests
To ensure that the handlers fulfill the [resource type handler contract](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html) requirements, you should run the full test suite after making any changes to the code.
To run the test, first run

```
sam local start-lambda
```

in the root directory to initialize a local Lambda instance (you may need to run `mvn package` first to include the latest changes in the JAR used by SAM) and then run

```
cfn test
```

to run the full contract test suite (this will use whatever credentials you have setup for the AWS CLI). Running all of the tests can take a few minutes, so if you want to focus on only a single test you can do so by running

```
cfn test -- -k <test_name>
```
