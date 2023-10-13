package com.sun.corba.se.spi.activation;


/**
 * com/sun/corba/se/spi/activation/_ServerImplBase.java .
 * Error!  A message was requested which does not exist.  The messages file does not contain the key: toJavaProlog1
 * Error!  A message was requested which does not exist.  The messages file does not contain the key: toJavaProlog2
 * Thursday, October 12, 2023 at 9:06:11 PM Central European Summer Time
 */


/** Server callback API, passed to Activator in active method.
 */
public abstract class _ServerImplBase extends org.omg.CORBA.portable.ObjectImpl
        implements com.sun.corba.se.spi.activation.Server, org.omg.CORBA.portable.InvokeHandler {

    // Constructors
    public _ServerImplBase() {
    }

    private static java.util.Map<String, Integer> _methods = new java.util.HashMap<String, Integer>();

    static {
        _methods.put("shutdown", 0);
        _methods.put("install", 1);
        _methods.put("uninstall", 2);
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
                                                       org.omg.CORBA.portable.InputStream in,
                                                       org.omg.CORBA.portable.ResponseHandler $rh) {
        org.omg.CORBA.portable.OutputStream out = null;
        java.lang.Integer __method = _methods.get($method);
        if (__method == null)
            throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

        switch (__method.intValue()) {

            /** Shutdown this server.  Returns after orb.shutdown() completes.
             */
            case 0:  // activation/Server/shutdown
            {
                this.shutdown();
                out = $rh.createReply();
                break;
            }


            /** Install the server.  Returns after the install hook completes
             * execution in the server.
             */
            case 1:  // activation/Server/install
            {
                this.install();
                out = $rh.createReply();
                break;
            }


            /** Uninstall the server.  Returns after the uninstall hook
             * completes execution.
             */
            case 2:  // activation/Server/uninstall
            {
                this.uninstall();
                out = $rh.createReply();
                break;
            }

            default:
                throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }

        return out;
    } // _invoke

    // Type-specific CORBA::Object operations
    private static String[] __ids = {
            "IDL:activation/Server:1.0"};

    public String[] _ids() {
        return (String[]) __ids.clone();
    }


} // class _ServerImplBase
