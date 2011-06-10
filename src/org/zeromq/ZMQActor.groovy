package org.zeromq
/**
 * User: Reuben Morais
 * Date: 09/06/11
 * Time: 10:25
 */

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * @author Alex Tkachman, Vaclav Pech
 */
private final class ForwardingDelegate extends GroovyObjectSupport {

    private final Object first;
    private final Object second;

    ForwardingDelegate(final Object first, final Object second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Object invokeMethod(final String name, final Object args) {
        try {
            return InvokerHelper.invokeMethod(first, name, args);
        }
        catch (MissingMethodException ignore) {
            return InvokerHelper.invokeMethod(second, name, args);
        }
    }

    @Override
    public Object getProperty(final String property) {
        try {
            return InvokerHelper.getProperty(first, property);
        }
        catch (MissingPropertyException ignore) {
            return InvokerHelper.getProperty(second, property);
        }
    }

    @Override
    public void setProperty(final String property, final Object newValue) {
        try {
            InvokerHelper.setProperty(first, property, newValue);
        }
        catch (MissingPropertyException ignore) {
            InvokerHelper.setProperty(second, property, newValue);
        }
    }
}

class ZMQActor {
    public ZMQActor(ZMQ.Context ctx, type) {
        socket = ctx?.socket(type);
        if (socket == null) {
            throw new Exception("Couldn't create 0MQ socket.")
        }
        socket.setLinger(0)

        poller = ctx.poller(1)
        poller.register(socket)

        terminatingFlag = false
    }

    void connect(String addr) {
        socket.connect(addr)
    }

    void bind(String addr) {
        socket.bind(addr)
    }

    void send(byte[] msg, flags = 0) {
        socket.send(msg, flags)
    }

    void send(String str) {
        send(str.getBytes())
    }

    void send(Integer i) {
        send(i.toString())
    }

    void reply(byte[] msg, flags = 0) {
        send(msg, flags)
    }

    void reply(String str) {
        reply(str.getBytes())
    }

    void reply(Integer i) {
        reply(i.toString())
    }

    byte[] recv() {
        socket.recv(0)
    }

    void onMessage(byte[] msg) {
        if (nextContinuation != null) {
            final Closure closure = nextContinuation
            nextContinuation = null
            closure.call(new String(msg))
        } else {
            throw new IllegalStateException("The actor " + this + " cannot handle the message " + msg + ", as it has no registered message handler at the moment.");
        }
    }

    protected void act() {
        throw new UnsupportedOperationException("The act method has not been overridden");
    }

    //TODO: support loop condition
    public final void loop(final Closure code) {
        checkForNull(code);
        checkForBodyArguments(code);
        final Closure enhancedClosure = enhanceClosure(code);
        this.loopClosure = enhancedClosure;

        assert nextContinuation == null;
        while (!terminatingFlag && nextContinuation == null) {
            enhancedClosure.call();
        }

        //TODO: support after loop code
    }

    public final void react(final Closure code) {
        react(-1L, code)
    }

    public final void react(final long timeout, final Closure code) {
        checkForNull(code)
        checkForMessageHandlerArguments(code)

        nextContinuation = enhanceClosure(code)

        if (timeout < -1L)
            timeout = -1L

        poller.poll(timeout)
        if (poller.pollin(0)) {
            onMessage(recv())
        }
    }

    public final void terminate() {
        terminatingFlag = true
    }

    public final void start() {
        go();
    }

    public final void run() {
        go();
    }

    public final ZMQActor go() {
        terminatingFlag = false
        act();
        return this
    }

    private static void checkForNull(final Runnable code) {
        if (code == null)
            throw new IllegalArgumentException("An actor's message handlers and loops cannot be set to a null value.")
    }

    private static void checkForBodyArguments(final Closure closure) {
        if (closure.getMaximumNumberOfParameters() > 1)
            throw new IllegalArgumentException("An actor's body as well as a body of a loop can only expect 0 arguments. " + closure.getMaximumNumberOfParameters() + " expected.")
    }

    private static void checkForMessageHandlerArguments(final Closure code) {
        if (code.getMaximumNumberOfParameters() > 1)
            throw new IllegalArgumentException("An actor's message handler can only expect 0 or 1 argument. " + code.getMaximumNumberOfParameters() + " expected.")
    }

    private static Closure enhanceClosure(final Closure closure) {
        final Closure cloned = (Closure) closure.clone();
        if (cloned.getOwner() == cloned.getDelegate()) {
            cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
            cloned.setDelegate(this);
        } else {
            cloned.setDelegate(new ForwardingDelegate(cloned.getDelegate(), this));
        }
        return cloned;
    }

    private boolean terminatingFlag
    private Runnable loopCode

    private Closure nextContinuation;
    private Closure loopClosure
    private ZMQ.Socket socket
    private ZMQ.Poller poller
}
