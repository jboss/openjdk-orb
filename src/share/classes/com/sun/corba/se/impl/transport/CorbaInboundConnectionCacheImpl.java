/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.corba.se.impl.transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sun.corba.se.pept.transport.Acceptor;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.InboundConnectionCache;

import com.sun.corba.se.spi.monitoring.LongMonitoredAttributeBase;
import com.sun.corba.se.spi.monitoring.MonitoringConstants;
import com.sun.corba.se.spi.monitoring.MonitoringFactories;
import com.sun.corba.se.spi.monitoring.MonitoredObject;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.orbutil.ORBUtility;

/**
 * @author Harold Carr
 */
public class CorbaInboundConnectionCacheImpl
    extends
        CorbaConnectionCacheBase
    implements
        InboundConnectionCache
{
    private List<Acceptor> acceptors;
    private List<Connection> connections;

    public CorbaInboundConnectionCacheImpl(ORB orb, String connectionCacheType, String monitoringName)
    {
        super(orb, connectionCacheType, monitoringName);
        this.acceptors = new ArrayList<>();
        this.connections = new ArrayList<>();
    }

    ////////////////////////////////////////////////////
    //
    // pept.transport.InboundConnectionCache
    //

    public void close () {

        super.close();
        for(Acceptor acceptor: acceptors) {
            if (orb.transportDebugFlag) {
                dprint(".close: " + acceptor);
            }
            acceptor.close();
        }

    }

    public void registerAcceptor(Acceptor acceptor){
        acceptors.add(acceptor);
    }

    public Collection<Acceptor> getAcceptors() {
        return acceptors;
    }

    public void add(Connection connection)
    {
        if (orb.transportDebugFlag) {
            dprint(".add: " + connection);
        }
        synchronized (backingStore()) {
            connections.add(connection);
            connection.setConnectionCache(this);
            dprintStatistics();
        }
    }

    public void remove(Connection connection)
    {
        if (orb.transportDebugFlag) {
            dprint(".remove: " +  connection);
        }
        synchronized (backingStore()) {
            connections.remove(connection);
            dprintStatistics();
        }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    public Collection values()
    {
        return connections;
    }

    protected Object backingStore()
    {
        return connections;
    }

    protected void registerWithMonitoring()
    {
        // ORB
        MonitoredObject orbMO =
            orb.getMonitoringManager().getRootMonitoredObject();

        // REVISIT - add ORBUtil mkdir -p like operation for this.

        // CONNECTION
        MonitoredObject connectionMO =
            orbMO.getChild(MonitoringConstants.CONNECTION_MONITORING_ROOT);
        if (connectionMO == null) {
            connectionMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        MonitoringConstants.CONNECTION_MONITORING_ROOT,
                        MonitoringConstants.CONNECTION_MONITORING_ROOT_DESCRIPTION);
            orbMO.addChild(connectionMO);
        }

        // INBOUND CONNECTION
        MonitoredObject inboundConnectionMO =
            connectionMO.getChild(
                MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT);
        if (inboundConnectionMO == null) {
            inboundConnectionMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT,
                        MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION);
            connectionMO.addChild(inboundConnectionMO);
        }

        // NODE FOR THIS CACHE
        MonitoredObject thisMO =
            inboundConnectionMO.getChild(getMonitoringName());
        if (thisMO == null) {
            thisMO =
                MonitoringFactories.getMonitoredObjectFactory()
                    .createMonitoredObject(
                        getMonitoringName(),
                        MonitoringConstants.CONNECTION_MONITORING_DESCRIPTION);
            inboundConnectionMO.addChild(thisMO);
        }

        LongMonitoredAttributeBase attribute;

        // ATTRIBUTE
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS,
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfConnections());
                }
            };
        thisMO.addAttribute(attribute);

        // ATTRIBUTE
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS,
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfIdleConnections());
                }
            };
        thisMO.addAttribute(attribute);

        // ATTRIBUTE
        attribute = new
            LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS,
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS_DESCRIPTION)
            {
                public Object getValue() {
                    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfBusyConnections());
                }
            };
        thisMO.addAttribute(attribute);
    }

    protected void dprint(String msg)
    {
        ORBUtility.dprint("CorbaInboundConnectionCacheImpl", msg);
    }
}

// End of file.
