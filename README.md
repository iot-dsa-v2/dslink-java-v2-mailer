# dslink-java-v2-mailer

* Java - version 1.6 and up.
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Overview

Link for sending e-mail alarms and notifications. 
Connect a Gmail or other SMTP server account, and use the "Send" action to send alerts and notifications.

If you are not familiar with DSA, an overview can be found at
[here](http://iot-dsa.org/get-started/how-dsa-works).

This link was built using the Java DSLink SDK which can be found
[here](https://github.com/iot-dsa-v2/sdk-dslink-java).

## Link Architecture

This section outlines the hierarchy of nodes defined by this link.

- _MainNode_ - Used to add new e-mail account connections.
  - _ConnectionNode_ - Used to send e-mails and manage the account.


## Node Guide

The following section provides detailed descriptions of each node in the link as well as
descriptions of actions, values and child nodes.


### MainNode

This is the root node of the link.  It has actions for connecting to new databases.

**Actions**
- Add SMTP Connection - Adds a connection to an SMTP e-mail account.
- Add Gmail Connection - Adds a SMTP connection preconfigured for Gmail.

**Values**
- Help - Link to this doc.

**Child Nodes**
- ConnectionNode - Connections configured for specific servers and accounts.

### ConnectionNode

Each connection node represents an outgoing email server and a specific
account for sending email through it.

**Actions**

- Send - Sends an e-mail. The parameters are:
    - To - One or more comma separated email addresses.  There must be at lease
      one of To, Cc, or Bcc.
    - Cc - One or more comma separated email addresses.  There must be at lease
      one of To, Cc, or Bcc.
    - Bcc - One or more comma separated email addresses.  There must be at lease
      one of To, Cc, or Bcc.
    - From - Optional, address of sender.
    - Subject - Optional, subject line text.
    - Body Text - Optional, plain text body message.
    - Attachment Name - Optional, only used if sending attachment.
    - Attachment Mime-Type - Optional, only used if sending attachment.  If not provide,
      the mime-type will be guess from the name and attachment bytes and defaults
      to application/octet-stream if it can't be determined.
    - Attachment Data - DSA Binary type representing the attachment.
- Delete - Delete the connection node

**Values**

- Enabled - When false, sending mail will result in an error.
- Host Name - Host name of the SMTP server.
- Last Fail - Last time sending failed.
- Last Ok - Last time sending succeeded.
- Password - Password for the given user.
- Port - SMTP server port
- SSL - Set to true if the server uses SSL rather than TLS
- Status - Options are disabled, down, fault and ok.  Down resents
  a send failure and fault is used for configuration errors.
- Status Text - Uses to describe fault or down conditions.
- User Name - User name used to login to the SMTP server

## Acknowledgements

SDK-DSLINK-JAVA

This software contains unmodified binary redistributions of 
[sdk-dslink-java](https://github.com/iot-dsa-v2/sdk-dslink-java), which is licensed 
and available under the Apache License 2.0. An original copy of the license agreement can be found 
at https://github.com/iot-dsa-v2/sdk-dslink-java/blob/master/LICENSE

## History

* Version 1.2.0
  - Connection node now subclasses DSBaseConnection from the SDK.
* Version 1.1.3
  - Grade update.
  - Fixes for latest SDK.
* Version 1.1.2
  - Dependency updates.
  - Change DSPasswordAes to DSPasswordAES128.
* Version 1.1.1
  - Dependency updates
* Version 1.1.0
  - Handle From, Cc and Bcc
  - Handle attachments
* Version 1.0.0
  - Connect to Gmail or other SMTP
  - Send e-mails using this connection
