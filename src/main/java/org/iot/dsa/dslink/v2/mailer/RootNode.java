package org.iot.dsa.dslink.v2.mailer;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * The root and only node of this link.
 *
 * @author Aaron Hansen
 */
public class RootNode extends DSRootNode implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static String COUNTER = "Counter";
    private static String DOCS = "Docs";
    private static String RESET = "Reset";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    // Nodes store children and meta-data about the relationship in DSInfo instances.
    // Storing infos as Java fields eliminates subsequent name lookups, but should only be
    // done with declared defaults.  It can be done with dynamic children, but extra
    // care will be required.
    private final DSInfo counter = getInfo(COUNTER);
    private final DSInfo reset = getInfo(RESET);

    private DSRuntime.Timer timer;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    // Nodes must support the public no-arg constructor.  Technically this isn't required here
    // since there are no other constructors...
    public RootNode() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Defines the permanent children of this node type, their existence is guaranteed in all
     * instances.  This is only ever called once per, type per process.
     */
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(COUNTER, DSInt.valueOf(0))
                .setTransient(true)
                .setReadOnly(true);
        declareDefault(RESET, DSAction.DEFAULT);
        // Change the following URL to your README
        declareDefault(DOCS, DSString.valueOf("https://github.com/iot-dsa-v2/dslink-java-v2-mailer"))
                .setTransient(true)
                .setReadOnly(true);
    }

    /**
     * Handles the reset action.
     */
    @Override
    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        if (actionInfo == reset) {
            synchronized (counter) {
                put(counter, DSInt.valueOf(0));
                // The following line would have also worked, but it would have
                // required a name lookup.
                // put(COUNTER, DSInt.valueOf(0));
            }
            return null;
        }
        return super.onInvoke(actionInfo, invocation);
    }

    /**
     * Starts the timer.
     */
    @Override
    protected void onSubscribed() {
        // Use DSRuntime for timers and its thread pool.
        timer = DSRuntime.run(this, System.currentTimeMillis() + 1000, 1000);
    }

    /**
     * Cancels an active timer if there is one.
     */
    @Override
    protected void onStopped() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Cancels the timer.
     */
    @Override
    protected void onUnsubscribed() {
        timer.cancel();
        timer = null;
    }

    /**
     * Called by the timer, increments the counter.
     */
    @Override
    public void run() {
        synchronized (counter) {
            DSInt value = (DSInt) counter.getValue();
            put(counter, DSInt.valueOf(value.toInt() + 1));
            // Without the counter field, this method would have required at least one lookup.
            // The following is the worst performance option (not that it really matters here):
            // DSInt val = (DSInt) get(COUNTER);
            // put(COUNTER, DSInt.valueOf(val.toInt() + 1));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
