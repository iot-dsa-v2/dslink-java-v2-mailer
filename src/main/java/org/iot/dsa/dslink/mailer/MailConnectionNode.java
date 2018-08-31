package org.iot.dsa.dslink.mailer;

import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.*;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.security.DSPasswordAes128;
import org.iot.dsa.util.DSException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Node representing an connection to an SMTP e-mail server.
 *
 * @author James (Juris) Puchin
 * Created on 10/17/2017
 */
public class MailConnectionNode extends DSNode {
    private final DSInfo usr_name = getInfo(Mailv2Helpers.E_USER);
    private final DSInfo password = getInfo(Mailv2Helpers.E_PASSWORD);
    private final DSInfo host = getInfo(Mailv2Helpers.HOST);
    private final DSInfo port = getInfo(Mailv2Helpers.PORT);

    @SuppressWarnings("WeakerAccess")
    public MailConnectionNode() {

    }

    MailConnectionNode(DSMap params) {
        setParameters(params);
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(Mailv2Helpers.E_USER, DSString.valueOf("No User"));
        declareDefault(Mailv2Helpers.E_PASSWORD, DSPasswordAes128.valueOf("No Pass")).setHidden(true);
        declareDefault(Mailv2Helpers.HOST, DSString.valueOf("No Host"));
        declareDefault(Mailv2Helpers.PORT, DSString.valueOf("587"));

        declareDefault(Mailv2Helpers.SEND_MAIL, makeSendMailAction());
        declareDefault(Mailv2Helpers.EDIT, makeEditAction());
        declareDefault(Mailv2Helpers.DELETE, makeDeleteAction());
    }

    private DSAction makeEditAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((MailConnectionNode) info.getParent()).edit(invocation.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.E_USER, DSValueType.STRING, null).setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.E_PASSWORD, DSValueType.STRING, null).setEditor("password").setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.HOST, DSValueType.STRING, null).setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.PORT, DSValueType.STRING, null).setPlaceHolder("Optional");
        return act;
    }

    private ActionResult edit(DSMap parameters) {
        setParameters(parameters);
        DSMainNode par = (DSMainNode) getParent();
        return null;
    }

    private DSAction makeSendMailAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((MailConnectionNode) info.getParent()).sendMail(invocation.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.TO, DSValueType.STRING, Mailv2Helpers.REC_DESC).setPlaceHolder("Need one To/Cc/Bcc");
        act.addParameter(Mailv2Helpers.CC, DSValueType.STRING, Mailv2Helpers.REC_DESC).setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.BCC, DSValueType.STRING, Mailv2Helpers.REC_DESC).setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.FROM, DSValueType.STRING, null).setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.SUBJ, DSValueType.STRING, null).setPlaceHolder(Mailv2Helpers.DEF_SUBJ);
        act.addParameter(Mailv2Helpers.BODY, DSValueType.STRING, null).setPlaceHolder(Mailv2Helpers.DEF_BODY);
        act.addParameter(Mailv2Helpers.ATTACH_NAME, DSValueType.STRING, Mailv2Helpers.ATTACH_DESC).setPlaceHolder("Attachment");
        act.addParameter(Mailv2Helpers.ATTACH_PATH, DSValueType.STRING, Mailv2Helpers.ATTACH_DESC).setPlaceHolder("Attachment");
        act.addParameter(Mailv2Helpers.ATTACH_BYTE, DSValueType.BINARY, Mailv2Helpers.ATTACH_DESC).setEditor("fileinput").setPlaceHolder("Attachment");
        return act;
    }

    private ActionResult sendMail(DSMap parameters) {
        String to = safeGetString(parameters, Mailv2Helpers.TO);
        String cc = safeGetString(parameters, Mailv2Helpers.CC);
        String bcc = safeGetString(parameters, Mailv2Helpers.BCC);
        if (to == null && cc == null && bcc == null)
            throw new DSRequestException("Need to specify an e-mail recipient!");

        String subj = safeGetString(parameters, Mailv2Helpers.SUBJ);
        String body = safeGetString(parameters, Mailv2Helpers.BODY);
        String from = safeGetString(parameters, Mailv2Helpers.FROM);
        String att_name = safeGetString(parameters, Mailv2Helpers.ATTACH_NAME);
        String att_path = safeGetString(parameters, Mailv2Helpers.ATTACH_PATH);
        DSBytes att_byte = DSBytes.NULL.valueOf(parameters.get(Mailv2Helpers.ATTACH_BYTE));
        executeSendMailTLS(to, subj, body, from, cc, bcc, att_name, att_path, att_byte);
        return null;
    }

    private static String safeGetString(DSMap para, String val) {
        String res = para.getString(val);
        if (res != null && res.equals("")) res = null;
        return res;
    }

    private DSAction makeDeleteAction() {
        return new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((MailConnectionNode) info.getParent()).delete();
                return null;
            }
        };
    }

    private void delete() {
        getParent().remove(getInfo());
    }

    private void setParameters(DSMap params) {
        if (!params.isNull(Mailv2Helpers.E_USER))
            put(usr_name, params.get(Mailv2Helpers.E_USER));
        if (!params.isNull(Mailv2Helpers.E_PASSWORD))
            setCurPass(params.get(Mailv2Helpers.E_PASSWORD).toString());
        if (!params.isNull(Mailv2Helpers.HOST))
            put(host, params.get(Mailv2Helpers.HOST));
        if (!params.isNull(Mailv2Helpers.PORT))
            put(port, params.get(Mailv2Helpers.PORT));
    }

    private String getCurPass() {
        return ((DSPasswordAes128) password.getValue()).decode();
    }

    private void setCurPass(String pass) {
        put(password, DSPasswordAes128.valueOf(pass));
    }

//    public static void main(String[] args) {
//        Session ses = connectToSerever(Cred.E_USER, Cred.E_PASS, Cred.GOOGLE_HOST, Cred.GOOGLE_PORT);
//        Message mes = new MimeMessage(ses);
//        try{
//        mes.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Cred.T_USER));
//
//             Transport.send(mes);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static Session connectToSerever(final String user, final String pass, String host, String port) {
        //Connect to server
        Properties props = new Properties();
        boolean ssl = false;
        if (ssl) {
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
        } else{
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
        }
        return Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                });
    }

    private void executeSendMailTLS(String to_mail, String subj, String body, String from,
                                    String cc, String bcc, String att_name, String att_path,
                                    DSBytes att_byte) {
        //Get node variables
        final String username = usr_name.getValue().toString();
        final String password = getCurPass();
        final String host = this.host.getValue().toString(); // ;
        final String port = this.port.getValue().toString(); //;

        Session session = connectToSerever(username, password, host, port);

        //Construct message
        try {
            Message message = new MimeMessage(session);
            if (from != null) message.setFrom(new InternetAddress(from));
            else message.setFrom(new InternetAddress(usr_name.getValue().toString()));

            if (to_mail != null) message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to_mail));
            if (cc != null) message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            if (bcc != null) message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));

            if (subj != null) message.setSubject(subj);
            else message.setSubject(Mailv2Helpers.DEF_SUBJ);

            MimeMultipart multipart = new MimeMultipart();

            //Deal with attachments
            if (att_name == null) att_name = "Attachment";
            if (!att_byte.isNull() && att_path != null) {
                warn("Two attachments sources specified, can only have one attachment!");
            } else {
                //Attach stream
                if (!att_byte.isNull()) {
                    String mime_type = null;
                    try {
                        //Guess type from data
                        mime_type = Mailv2Helpers.guessMimeType(att_byte.getBytes());
                    } catch (IOException e) {
                        //Will create warning if both methods fail
                        warn("Failed to guess stream data type, will use file name");
                    }

                    if (mime_type == null) {
                        //Guess type from extension, default to "application/octet-stream"
                        mime_type = new MimetypesFileTypeMap().getContentType(att_name);
                    }

                    MimeBodyPart att_b = new MimeBodyPart();
                    ByteArrayDataSource att_ds = new ByteArrayDataSource(att_byte.getBytes(), mime_type);
                    att_b.setDataHandler(new DataHandler(att_ds));
                    att_b.setFileName(att_name);
                    multipart.addBodyPart(att_b);
                }
                //Attach by path
                else if (att_path != null) {
                    DataSource att_ds = new FileDataSource(att_path);
                    MimeBodyPart att_b = new MimeBodyPart();
                    att_b.setDataHandler(new DataHandler(att_ds));
                    att_b.setFileName(att_name);
                    multipart.addBodyPart(att_b);
                }
            }

            //Create e-mail body
            if (body == null) body = Mailv2Helpers.DEF_BODY;
            MimeBodyPart msg_body = new MimeBodyPart();
            msg_body.setText(body);
            multipart.addBodyPart(msg_body);

            //Send
            message.setContent(multipart);
            Transport.send(message);
            info("E-mail Sent!");

        } catch (MessagingException e) {
            warn("Failed to send e-mail ", e);
            DSException.throwRuntime(e);
        }
    }
}
