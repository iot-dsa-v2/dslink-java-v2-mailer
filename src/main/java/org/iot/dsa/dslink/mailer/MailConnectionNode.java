package org.iot.dsa.dslink.mailer;

import java.io.IOException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.iot.dsa.conn.DSBaseConnection;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.security.DSPasswordAes128;
import org.iot.dsa.util.DSException;

/**
 * Node representing a connection to a SMTP server.
 *
 * @author James (Juris) Puchin
 * Created on 10/17/2017
 */
public class MailConnectionNode extends DSBaseConnection {

    private final DSInfo enabled = getInfo(Mailv2Helpers.ENABLED);
    private final DSInfo host = getInfo(Mailv2Helpers.HOST);
    private MimetypesFileTypeMap mimeTypeMap;
    private final DSInfo password = getInfo(Mailv2Helpers.PASSWORD);
    private final DSInfo port = getInfo(Mailv2Helpers.PORT);
    private final DSInfo ssl = getInfo(Mailv2Helpers.SSL);
    private final DSInfo userName = getInfo(Mailv2Helpers.USER_NAME);

    public MailConnectionNode() {
    }

    MailConnectionNode(DSMap params) {
        setParameters(params);
    }

    @Override
    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    @Override
    protected void checkConfig() {
        if (userName.getElement().toString().isEmpty()) {
            throw new IllegalStateException("Empty username");
        }
        if (host.getElement().toString().isEmpty()) {
            throw new IllegalStateException("Empty hostname");
        }
        configOk();
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(Mailv2Helpers.ENABLED, DSBool.TRUE);
        declareDefault(Mailv2Helpers.USER_NAME, DSString.valueOf(""));
        declareDefault(Mailv2Helpers.PASSWORD, DSPasswordAes128.NULL);
        declareDefault(Mailv2Helpers.HOST, DSString.valueOf(""));
        declareDefault(Mailv2Helpers.PORT, DSLong.valueOf(587));
        declareDefault(Mailv2Helpers.SSL, DSBool.valueOf(false));
        declareDefault(Mailv2Helpers.SEND_MAIL, makeSendMailAction());
        declareDefault(Mailv2Helpers.DELETE, makeDeleteAction());
        put("Status", DSStatus.ok); //TODO - change to constant in next sdk release
    }

    @Override
    protected void onChildChanged(DSInfo child) {
        if (child == enabled) {
            canConnect(); //update disabled status
        }
        super.onChildChanged(child);
    }

    private static Session connectToServer(final String user,
                                           final String pass,
                                           String host,
                                           int port,
                                           boolean ssl) {
        Properties props = new Properties();
        if (ssl) {
            props.put("mail.smtp.ssl.enable", true);
        }
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        return Session.getInstance(props,
                                   new javax.mail.Authenticator() {
                                       protected PasswordAuthentication getPasswordAuthentication() {
                                           return new PasswordAuthentication(user, pass);
                                       }
                                   });
    }

    private void delete() {
        getParent().remove(getInfo());
    }

    private void executeSend(String to_mail, String subj, String body, String from, String cc,
                             String bcc, String att_name, DSBytes att_byte, String mimeType) {
        try {
            Session session = connectToServer(
                    userName.getElement().toString(),
                    getCurPass(),
                    host.getElement().toString(),
                    port.getElement().toInt(),
                    ssl.getElement().toBoolean());
            Message message = new MimeMessage(session);
            if (from != null) {
                message.setFrom(new InternetAddress(from));
            } else {
                message.setFrom(new InternetAddress(userName.getValue().toString()));
            }
            if (to_mail != null) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to_mail));
            }
            if (cc != null) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            }
            if (bcc != null) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
            }
            if (subj != null) {
                message.setSubject(subj);
            } else {
                message.setSubject(Mailv2Helpers.DEF_SUBJ);
            }
            MimeMultipart multipart = new MimeMultipart();
            if (!att_byte.isNull()) {
                try {
                    //Guess type from data and name
                    if ((mimeType == null) || mimeType.isEmpty()) {
                        mimeType = Mailv2Helpers.guessMimeType(att_name, att_byte.getBytes());
                    }
                } catch (IOException e) {
                    warn("Failed to guess attachment mime type.");
                }
                if ((mimeType == null) || mimeType.isEmpty()) {
                    if (att_name != null) {
                        if (mimeTypeMap == null) {
                            mimeTypeMap = new MimetypesFileTypeMap();
                        }
                        mimeType = mimeTypeMap.getContentType(att_name);
                    } else {
                        mimeType = "application/octet-stream";
                    }
                }
                if (att_name == null) {
                    att_name = "Attachment";
                }
                MimeBodyPart att_b = new MimeBodyPart();
                ByteArrayDataSource att_ds = new ByteArrayDataSource(
                        att_byte.getBytes(), mimeType);
                att_b.setDataHandler(new DataHandler(att_ds));
                att_b.setFileName(att_name);
                multipart.addBodyPart(att_b);
            }
            //Body
            if (body == null) {
                body = Mailv2Helpers.DEF_BODY;
            }
            MimeBodyPart msg_body = new MimeBodyPart();
            msg_body.setText(body);
            multipart.addBodyPart(msg_body);
            //Send
            message.setContent(multipart);
            Transport.send(message);
            debug(debug() ? "Email sent " + getPath() : null);
        } catch (Exception e) {
            error(getPath(), e);
            DSException.throwRuntime(e);
        }
    }

    private String getCurPass() {
        return ((DSPasswordAes128) password.getValue()).decode();
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

    private DSAction makeSendMailAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((MailConnectionNode) info.getParent()).sendMail(invocation.getParameters());
            }
        };
        String desc = "Need one of To, Cc or Bcc";
        act.addParameter(Mailv2Helpers.TO, DSValueType.STRING, Mailv2Helpers.REC_DESC)
           .setPlaceHolder(desc);
        act.addParameter(Mailv2Helpers.CC, DSValueType.STRING, Mailv2Helpers.REC_DESC)
           .setPlaceHolder(desc);
        act.addParameter(Mailv2Helpers.BCC, DSValueType.STRING, Mailv2Helpers.REC_DESC)
           .setPlaceHolder(desc);
        act.addParameter(Mailv2Helpers.FROM, DSValueType.STRING, null)
           .setPlaceHolder("Optional");
        act.addParameter(Mailv2Helpers.SUBJ, DSValueType.STRING, null)
           .setPlaceHolder(Mailv2Helpers.DEF_SUBJ);
        act.addParameter(Mailv2Helpers.BODY, DSValueType.STRING, "Plain text message")
           .setPlaceHolder(Mailv2Helpers.DEF_BODY);
        desc = "Optional and only used if sending attachment";
        act.addParameter(Mailv2Helpers.ATTACH_NAME, DSValueType.STRING, desc)
           .setPlaceHolder(desc);
        String desc2 = "Optional, guessed using name and data if not provided";
        act.addParameter(Mailv2Helpers.ATTACH_MIME_TYPE, DSValueType.STRING, desc2)
           .setPlaceHolder(desc2);
        act.addParameter(Mailv2Helpers.ATTACH_DATA, DSValueType.BINARY, desc)
           .setEditor("fileinput");
        return act;
    }

    private static String safeGetString(DSMap para, String val) {
        String res = para.getString(val);
        if (res != null && res.isEmpty()) {
            res = null;
        }
        return res;
    }

    private ActionResult sendMail(DSMap parameters) {
        if (!canConnect()) {
            throw new RuntimeException("Cannot send email");
        }
        try {
            String to = safeGetString(parameters, Mailv2Helpers.TO);
            String cc = safeGetString(parameters, Mailv2Helpers.CC);
            String bcc = safeGetString(parameters, Mailv2Helpers.BCC);
            if (to == null && cc == null && bcc == null) {
                throw new DSRequestException("Need to specify an e-mail recipient!");
            }
            String subj = safeGetString(parameters, Mailv2Helpers.SUBJ);
            String body = safeGetString(parameters, Mailv2Helpers.BODY);
            String from = safeGetString(parameters, Mailv2Helpers.FROM);
            String att_name = safeGetString(parameters, Mailv2Helpers.ATTACH_NAME);
            DSBytes att_byte = DSBytes.NULL.valueOf(parameters.get(Mailv2Helpers.ATTACH_DATA));
            String mimeType = safeGetString(parameters, Mailv2Helpers.ATTACH_MIME_TYPE);
            executeSend(to, subj, body, from, cc, bcc, att_name, att_byte, mimeType);
            connOk();
        } catch (Exception x) {
            error(getPath(), x);
            connDown(DSException.makeMessage(x));
        }
        return null;
    }

    private void setCurPass(String pass) {
        put(password, DSPasswordAes128.valueOf(pass));
    }

    private void setParameters(DSMap params) {
        if (!params.isNull(Mailv2Helpers.USER_NAME)) {
            put(userName, params.get(Mailv2Helpers.USER_NAME));
        }
        if (!params.isNull(Mailv2Helpers.PASSWORD)) {
            setCurPass(params.get(Mailv2Helpers.PASSWORD).toString());
        }
        if (!params.isNull(Mailv2Helpers.HOST)) {
            put(host, params.get(Mailv2Helpers.HOST));
        }
        if (!params.isNull(Mailv2Helpers.PORT)) {
            put(port, params.get(Mailv2Helpers.PORT));
        }
    }

}
