package org.iot.dsa.dslink.mailer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 * Container for various helper variables and/or functions.
 *
 * @author James (Juris) Puchin
 * Created on 10/17/2017
 */
class Mailv2Helpers {

    ////////////////////
    //String Definitions
    ////////////////////

    static final String ADD_SMTP = "Add SMTP Connection";
    static final String ADD_GMAIL = "Add Gmail Connection";

    static final String DEF_SUBJ = "DSA Generated Email";
    static final String DEF_BODY = "This email was auto generated by the mailer link.";
    static final String HELP = "Help";

    static final String NODE_NAME = "Node Name";
    static final String USER_NAME = "User Name";
    static final String PASSWORD = "Password";
    static final String HOST = "Host Name";
    static final String PORT = "Port";
    static final String SSL = "SSL";
    static final String TIMEOUT = "Timeout";

    static final String ENABLED = "Enabled";
    static final String DELETE = "Delete";

    static final String REC_DESC = "Comma separated list of recipients.";
    static final String TO = "To";
    static final String CC = "Cc";
    static final String BCC = "Bcc";
    static final String FROM = "From";
    static final String SUBJ = "Subject";
    static final String BODY = "Body Text";
    static final String ATTACH_NAME = "Attachment Name";
    static final String ATTACH_DATA = "Attachment Data";
    static final String ATTACH_MIME_TYPE = "Attachment Mime-Type";
    static final String SEND_MAIL = "Send";

    ////////////////////
    //Helper Methods
    ////////////////////

    static String guessMimeType(String name, byte[] in) throws IOException {
        String ret = null;
        if ((name != null) && !name.isEmpty()) {
            ret = URLConnection.guessContentTypeFromName(name);
        }
        if ((ret != null) && !ret.isEmpty()) {
            return ret;
        }
        if ((in != null) && (in.length > 0)) {
            ret = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(in));
        }
        return ret;
    }

}
