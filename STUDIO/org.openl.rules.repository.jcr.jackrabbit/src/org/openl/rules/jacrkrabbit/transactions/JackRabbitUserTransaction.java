/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openl.rules.jacrkrabbit.transactions;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.XASession;
import org.apache.jackrabbit.rmi.client.ClientXASession;

/**
 * Internal JackRabbit {@link javax.transaction.UserTransaction} implementation.
 */
public class JackRabbitUserTransaction implements UserTransaction {

    /**
     * Global transaction id counter
     */
    private static byte counter = 0;

    /**
     * The XAResources map
     */
    private Map<XAResource, Xid> xaResources = new HashMap<XAResource, Xid>();

    /**
     * Status
     */
    private int status = Status.STATUS_NO_TRANSACTION;

    private boolean distributedThreadAccess = false;
    
    /**
     * Create a new instance of this class. Takes a session as parameter.
     * @param session session. If session is not of type
     * {@link XASession}, an <code>IllegalArgumentException</code>
     * is thrown
     */
    public JackRabbitUserTransaction(Session session) {
        this(session, false);
    }

    /**
     * Create a new instance of this class. Takes a session as parameter.
     * @param session session. If session is not of type
     * {@link XASession}, an <code>IllegalArgumentException</code>
     * is thrown
     */
    public JackRabbitUserTransaction(Session session, boolean distributedThreadAccess) {
        if (session instanceof XASession) {
            counter++;
            xaResources.put(((XASession) session).getXAResource(), new XidImpl(counter));
            this.distributedThreadAccess = distributedThreadAccess; 
        } else if (session instanceof ClientXASession) {
            counter++;
            xaResources.put((ClientXASession) session, new XidImpl(counter));
            this.distributedThreadAccess = distributedThreadAccess; 
        } else {
            throw new IllegalArgumentException("Session not of type XASession");
        }
    }

    /**
     * Enlists the given Session to this UserTransaction
     */
    public void enlistXAResource(Session session) {
        xaResources.put((XAResource) session, new XidImpl(counter));
    }

    /**
     * @see javax.transaction.UserTransaction#begin
     */
    public void begin() throws NotSupportedException, SystemException {
//        if (status != Status.STATUS_NO_TRANSACTION) {
//            throw new IllegalStateException("Transaction already active");
//        }

        try {
            for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                entry.getKey().start(entry.getValue(), XAResource.TMNOFLAGS);
            }
            status = Status.STATUS_ACTIVE;

        } catch (XAException e) {

            throw new SystemException("Unable to begin transaction: " +
                    "XA_ERR=" + e.errorCode);
        }
    }

    /**
     * @see javax.transaction.UserTransaction#commit
     */
    public void commit() throws HeuristicMixedException,
            HeuristicRollbackException, IllegalStateException,
            RollbackException, SecurityException, SystemException {

        if (status != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("Transaction not active");
        }

        try {
            for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                entry.getKey().end(entry.getValue(), XAResource.TMSUCCESS);
            }

            status = Status.STATUS_PREPARING;
            for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                entry.getKey().prepare(entry.getValue());
            }
            status = Status.STATUS_PREPARED;

            status = Status.STATUS_COMMITTING;
            if (distributedThreadAccess) {
                Thread distributedThread = new Thread() {
                    public void run() {
                        try {
                            for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                                entry.getKey().commit(entry.getValue(), false);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                };
                distributedThread.start();
                distributedThread.join(1000);
                if (distributedThread.isAlive()) {
                    throw new SystemException(
                            "Commit from different thread but same XID must not block");
                }
            } else {
                for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                    entry.getKey().commit(entry.getValue(), false);
                }
            }
            
            status = Status.STATUS_COMMITTED;

        } catch (XAException e) {

            if (e.errorCode >= XAException.XA_RBBASE &&
                    e.errorCode <= XAException.XA_RBEND) {
                RollbackException re = new RollbackException(
                        "Transaction rolled back: XA_ERR=" + e.errorCode);
                re.initCause(e.getCause());
                throw re;
            } else {
                SystemException se = new SystemException(
                        "Unable to commit transaction: XA_ERR=" + e.errorCode);
                se.initCause(e.getCause());
                throw se;
            }
        } catch (InterruptedException e) {
            throw new SystemException("Thread.join() interrupted");
        }
    }

    /**
     * @see javax.transaction.UserTransaction#getStatus
     */
    public int getStatus() throws SystemException {
        return status;
    }

    /**
     * @see javax.transaction.UserTransaction#rollback
     */
    public void rollback() throws IllegalStateException, SecurityException,
            SystemException {

        if (status != Status.STATUS_ACTIVE &&
                status != Status.STATUS_MARKED_ROLLBACK) {

            throw new IllegalStateException("Transaction not active");
        }

        try {
            for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                entry.getKey().end(entry.getValue(), XAResource.TMFAIL);
            }

            status = Status.STATUS_ROLLING_BACK;
            for (Map.Entry<XAResource, Xid> entry : xaResources.entrySet()) {
                entry.getKey().rollback(entry.getValue());
            }
            status = Status.STATUS_ROLLEDBACK;

        } catch (XAException e) {
            SystemException se = new SystemException(
                    "Unable to rollback transaction: XA_ERR=" + e.errorCode);
            se.initCause(e.getCause());
            throw se;
        }
    }

    /**
     * @see javax.transaction.UserTransaction#setRollbackOnly()
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (status != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("Transaction not active");
        }
        status = Status.STATUS_MARKED_ROLLBACK;
    }

    /**
     * @see javax.transaction.UserTransaction#setTransactionTimeout
     */
    public void setTransactionTimeout(int seconds) throws SystemException {
        try {
            for (XAResource xaResource : xaResources.keySet()) {
                xaResource.setTransactionTimeout(seconds);
            }
        } catch (XAException e) {
            SystemException se = new SystemException(
                    "Unable to set the TransactionTiomeout: XA_ERR=" + e.errorCode);
            se.initCause(e.getCause());
            throw se;
        }
    }


    /**
     * Internal {@link Xid} implementation.
     */
    static class XidImpl implements Xid {

        /** Global transaction id */
        private final byte[] globalTxId;

        /**
         * Create a new instance of this class. Takes a global
         * transaction number as parameter
         * @param globalTxNumber global transaction number
         */
        public XidImpl(byte globalTxNumber) {
            this.globalTxId = new byte[] { globalTxNumber };
        }

        /**
         * @see javax.transaction.xa.Xid#getFormatId()
         */
        public int getFormatId() {
            return 0;
        }

        /**
         * @see javax.transaction.xa.Xid#getBranchQualifier()
         */
        public byte[] getBranchQualifier() {
            return new byte[0];
        }

        /**
         * @see javax.transaction.xa.Xid#getGlobalTransactionId()
         */
        public byte[] getGlobalTransactionId() {
            return globalTxId;
        }
    }
}
