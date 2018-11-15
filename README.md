# dslink-java-v2-mailer

* Java - version 1.8 and up.
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Overview

Link for sending e-mail via SMTP.

If you are not familiar with DSA, an overview can be found at
[here](http://iot-dsa.org/get-started/how-dsa-works).

This link was built using the Java DSLink SDK which can be found
[here](https://github.com/iot-dsa-v2/sdk-dslink-java).

## Link Architecture

These are nodes defined by this link:

- _MainNode_ - Used to add new e-mail account connections.
  - _ConnectionNode_ - Used to send e-mail.


## Node Guide

The following section provides descriptions of nodes in the link as well as
descriptions of actions, values and child nodes.


### MainNode

This is the root node of the link.  Use it to create connections to email servers.

**Actions**
- Add SMTP Connection - Adds a connection to an SMTP e-mail account.  The
  parameters are:
  - Node Name - What to name the connection node in the tree.
  - User Name - User account used to authenticate with the smtp server.
  - Password - Password for the user account.
  - Host Name - SMTP server host or IP.
  - Port - SMTP server port.
- Add Gmail Connection - Adds a SMTP connection preconfigured for Gmail.  The
  parameters are:
  - Node Name - What to name the connection node in the tree.
  - User Name - User account used to authenticate with the smtp server.
  - Password - This should be an application password, they can be
    generated in Gmail account settings.

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
    - Attachment Mime-Type - Optional, only used if sending attachment.  If not provided,
      the mime-type will be guessed from the name and attachment bytes and defaults
      to application/octet-stream if it can't be determined.
    - Attachment Data - DSA Binary type representing the attachment.
- Delete - Delete the connection node

**Values**

- Enabled - When false, sending mail will result in an error.
- Host Name - Host name of the SMTP server.
- Last Fail - Last time sending failed.
- Last Ok - Last time sending succeeded.
- Password - Password for the given username.  For Gmail this should
  be an application password.
- Port - SMTP server port
- SSL - Set to true if server port explicitly uses SSL/TLS.  The link
  will attempt to upgrade to SSL/TLS no matter what.
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
