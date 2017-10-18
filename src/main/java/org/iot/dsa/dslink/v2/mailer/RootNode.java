package org.iot.dsa.dslink.v2.mailer;

import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.*;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Root node for the mailer DSLink
 *
 * @author James (Juris) Puchin
 * Created on 10/17/2017
 */
public class RootNode extends DSRootNode {
    //testing branch
    // Nodes must support the public no-arg constructor.  Technically this isn't required here
    // since there are no other constructors...
    public RootNode() {
    }

    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(Mailv2Helpers.DOCS, DSString.valueOf("https://github.com/iot-dsa-v2/dslink-java-v2-mailer"))
                .setTransient(true)
                .setReadOnly(true);
        declareDefault(Mailv2Helpers.ADD_SMTP, makeAddSMTPAction());
        declareDefault(Mailv2Helpers.ADD_GMAIL, makeAddGmailAction());
    }

    private DSAction makeAddSMTPAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((RootNode) info.getParent()).addSMTP(invocation.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.CON_NAME, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.E_USER, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.E_PASSWORD, DSValueType.STRING, null).setEditor("password");
        act.addParameter(Mailv2Helpers.HOST, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.PORT, DSValueType.STRING, null).setDefault(DSString.valueOf("587"));
        return act;
    }

    private ActionResult addSMTP(DSMap parameters) {
        DSNode nextDB = new MailConnectionNode(parameters);
        add(parameters.getString(Mailv2Helpers.CON_NAME), nextDB);
        getLink().save();
        return null;
    }

    private DSAction makeAddGmailAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((RootNode) info.getParent()).addGmail(invocation.getParameters());
            }
        };
        act.addParameter(Mailv2Helpers.CON_NAME, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.E_USER, DSValueType.STRING, null);
        act.addParameter(Mailv2Helpers.E_PASSWORD, DSValueType.STRING, null).setEditor("password");
        return act;
    }

    private ActionResult addGmail(DSMap parameters) {
        parameters.put(Mailv2Helpers.HOST, DSString.valueOf("smtp.gmail.com"));
        parameters.put(Mailv2Helpers.PORT, DSString.valueOf("587"));
        DSNode nextDB = new MailConnectionNode(parameters);
        add(parameters.getString(Mailv2Helpers.CON_NAME), nextDB);
        getLink().save();
        return null;
    }
}