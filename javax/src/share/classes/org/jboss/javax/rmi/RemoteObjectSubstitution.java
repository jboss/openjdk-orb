package org.jboss.javax.rmi;

/**
 *
 * Interface that allows remote objects to be substituted for a different remote object
 * before being written to the stream.
 *
 * This was introduced to allow jboss-ejb-client proxies to be swapped out for their corresponding IIOP stubs
 * 
 * @author Stuart Douglas
 */
public interface RemoteObjectSubstitution {

    Object writeReplaceRemote(Object other);

}
