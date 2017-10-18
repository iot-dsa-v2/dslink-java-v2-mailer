package org.iot.dsa.dslink.v2.mailer;

import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.*;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.security.DSPasswordAes;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
        declareDefault(Mailv2Helpers.E_PASSWORD, DSPasswordAes.valueOf("No Pass")).setHidden(true);
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
        act.addParameter(Mailv2Helpers.E_USER, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.E_PASSWORD, DSValueType.STRING, null).setEditor("password");
        act.addParameter(Mailv2Helpers.HOST, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.PORT, DSValueType.STRING, null);
        return act;
    }

    private ActionResult edit(DSMap parameters) {
        setParameters(parameters);
        DSRootNode par = (DSRootNode) getParent();
        par.getLink().save();
        return null;
    }

    private DSAction makeSendMailAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((MailConnectionNode) info.getParent()).sendMail(invocation.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.TO, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.SUBJ, DSValueType.STRING, null).setDefault(DSString.valueOf("Alert"));
        act.addParameter(Mailv2Helpers.BODY, DSValueType.STRING, null);
        return act;
    }

    private ActionResult sendMail(DSMap parameters) {
        String to = parameters.get(Mailv2Helpers.TO).toString();
        String subj = parameters.get(Mailv2Helpers.SUBJ).toString();
        String body = parameters.get(Mailv2Helpers.BODY).toString();
        executeSendMailTLS(to, subj, body);
        return null;
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
        return ((DSPasswordAes) password.getValue()).decode();
    }

    private void setCurPass(String pass) {
        put(password, DSPasswordAes.valueOf(pass));
    }

    private void executeSendMailTLS(String to_mail, String subj, String body) {
        final String username = usr_name.getValue().toString();
        final String password = getCurPass();
        final String host = this.host.getValue().toString(); // "smtp.gmail.com";
        final String port = this.port.getValue().toString(); //"587";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to_mail));
            message.setSubject(subj);
            message.setText(body);

            Transport.send(message);

            info("E-mail Sent!");

        } catch (MessagingException e) {
            warn("Failed to send e-mail ", e);
        }
    }
}
