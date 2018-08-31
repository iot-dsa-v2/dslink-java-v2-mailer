# dslink-java-v2-mailer

* Java - version 1.6 and up.
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Overview

Link for sending e-mail alarms and notifications. 
Connect a Gmail or other SMTP server account, and use the "Send Mail" action to send alerts and notifications.

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
- Connect SMTP - Connects to an SMTP e-mail account.
- Connect Gmail - Connects to a Gmail account.

**Values**
- Docs - Link to the GitHub Documentation

**Child Nodes**
- ConnectionNode - A new connection node is created for each new e-mail account

### ConnectionNode

Each connection node represents a new e-mail account connection.

**Actions**

- Send Mail - Send an e-mail
- Edit - Edit the account settings
- Disconnect - Disconnect from the account

**Values**

- Host Name - Host name of the SMTP server
- Port - SMTP server port
- User Name - User name used to login to the SMTP server

## Acknowledgements

SDK-DSLINK-JAVA

This software contains unmodified binary redistributions of 
[sdk-dslink-java](https://github.com/iot-dsa-v2/sdk-dslink-java), which is licensed 
and available under the Apache License 2.0. An original copy of the license agreement can be found 
at https://github.com/iot-dsa-v2/sdk-dslink-java/blob/master/LICENSE

## History

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
