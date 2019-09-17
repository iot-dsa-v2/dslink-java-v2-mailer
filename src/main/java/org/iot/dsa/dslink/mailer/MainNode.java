package org.iot.dsa.dslink.mailer;

import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

/**
 * Root node for the mailer DSLink
 *
 * @author James (Juris) Puchin
 * Created on 10/17/2017
 */
public class MainNode extends DSMainNode {

    public MainNode() {
    }

    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(Mailv2Helpers.HELP, DSString.valueOf(
                "https://github.com/iot-dsa-v2/dslink-java-v2-mailer#dslink-java-v2-mailer"))
                .setTransient(true)
                .setReadOnly(true);
        declareDefault(Mailv2Helpers.ADD_SMTP, makeAddSMTPAction()).setTransient(true);
        declareDefault(Mailv2Helpers.ADD_GMAIL, makeAddGmailAction()).setTransient(true);
    }

    private ActionResults addGmail(DSMap parameters) {
        parameters.put(Mailv2Helpers.HOST, DSString.valueOf("smtp.gmail.com"));
        parameters.put(Mailv2Helpers.PORT, DSLong.valueOf(587));
        DSNode nextDB = new MailConnectionNode(parameters);
        add(parameters.getString(Mailv2Helpers.NODE_NAME), nextDB);
        return null;
    }

    private ActionResults addSMTP(DSMap parameters) {
        DSNode nextDB = new MailConnectionNode(parameters);
        add(parameters.getString(Mailv2Helpers.NODE_NAME), nextDB);
        return null;
    }

    private DSAction makeAddGmailAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((MainNode) req.getTarget()).addGmail(req.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.NODE_NAME, DSString.NULL, null)
           .setDefault(DSString.valueOf("Gmail"));
        act.addParameter(Mailv2Helpers.USER_NAME, DSString.NULL, null);
        String desc = "Get an application password";
        act.addParameter(Mailv2Helpers.PASSWORD, DSString.NULL, desc)
           .setPlaceHolder(desc)
           .setEditor("password");
        return act;
    }

    private DSAction makeAddSMTPAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((MainNode) req.getTarget()).addSMTP(req.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.NODE_NAME, DSString.NULL, null)
           .setDefault(DSString.valueOf("SMTP"));
        act.addParameter(Mailv2Helpers.USER_NAME, DSString.NULL, null);
        act.addParameter(Mailv2Helpers.PASSWORD, DSString.NULL, null)
           .setEditor("password");
        act.addParameter(Mailv2Helpers.HOST, DSString.NULL, null);
        act.addDefaultParameter(Mailv2Helpers.PORT, DSLong.valueOf(587), null);
        return act;
    }
}
