# AWS::VoiceID::Domain Domain

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#domainid" title="DomainId">DomainId</a>" : <i>String</i>,
    "<a href="#arn" title="Arn">Arn</a>" : <i>String</i>,
    "<a href="#name" title="Name">Name</a>" : <i>String</i>,
    "<a href="#description" title="Description">Description</a>" : <i>String</i>,
    "<a href="#domainstatus" title="DomainStatus">DomainStatus</a>" : <i>String</i>,
    "<a href="#serversideencryptionconfiguration" title="ServerSideEncryptionConfiguration">ServerSideEncryptionConfiguration</a>" : <i><a href="serversideencryptionconfiguration.md">ServerSideEncryptionConfiguration</a></i>,
    "<a href="#createdat" title="CreatedAt">CreatedAt</a>" : <i>String</i>,
    "<a href="#updatedat" title="UpdatedAt">UpdatedAt</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#domainid" title="DomainId">DomainId</a>: <i>String</i>
<a href="#arn" title="Arn">Arn</a>: <i>String</i>
<a href="#name" title="Name">Name</a>: <i>String</i>
<a href="#description" title="Description">Description</a>: <i>String</i>
<a href="#domainstatus" title="DomainStatus">DomainStatus</a>: <i>String</i>
<a href="#serversideencryptionconfiguration" title="ServerSideEncryptionConfiguration">ServerSideEncryptionConfiguration</a>: <i><a href="serversideencryptionconfiguration.md">ServerSideEncryptionConfiguration</a></i>
<a href="#createdat" title="CreatedAt">CreatedAt</a>: <i>String</i>
<a href="#updatedat" title="UpdatedAt">UpdatedAt</a>: <i>String</i>
</pre>

## Properties

#### DomainId

_Required_: No

_Type_: String

_Minimum_: <code>22</code>

_Maximum_: <code>22</code>

_Pattern_: <code>^[a-zA-Z0-9]{22}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Arn

_Required_: No

_Type_: String

_Pattern_: <code>^arn:aws(-[^:]+)?:voiceid.+:[0-9]{12}:domain/[a-zA-Z0-9]{22}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^[a-zA-Z0-9][a-zA-Z0-9_-]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Pattern_: <code>^([a-zA-Z0-9\s_.:/=+\-%@]*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DomainStatus

_Required_: No

_Type_: String

_Allowed Values_: <code>ACTIVE</code> | <code>PENDING</code> | <code>SUSPENDED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ServerSideEncryptionConfiguration

_Required_: No

_Type_: <a href="serversideencryptionconfiguration.md">ServerSideEncryptionConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CreatedAt

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UpdatedAt

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
