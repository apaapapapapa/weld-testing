package com.example.util;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.weld.transaction.spi.TransactionServices;

public class CDITransactionServices implements TransactionServices {
    private static final Logger LOG = Logger.getLogger(CDITransactionServices.class);

    @Override
    public void registerSynchronization(Synchronization synchronizedObserver) {
        try {
            com.arjuna.ats.jta.TransactionManager.transactionManager()
                .getTransaction().registerSynchronization(synchronizedObserver);
        } catch (SystemException | IllegalStateException | RollbackException e) {
            throw new IllegalStateException("Cannot register synchronization observer " + synchronizedObserver
                    + " to the available transaction", e);
        }
    }

    @Override
    public boolean isTransactionActive() {
        try {
            int status = com.arjuna.ats.jta.TransactionManager.transactionManager().getStatus();
            switch(status) {
                case Status.STATUS_ACTIVE:
                case Status.STATUS_COMMITTING:
                case Status.STATUS_MARKED_ROLLBACK:
                case Status.STATUS_PREPARED:
                case Status.STATUS_PREPARING:
                case Status.STATUS_ROLLING_BACK:
                    return true;
                default:
                    return false;
            }
        } catch (SystemException se) {
            LOG.error("Cannot obtain the status of the transaction", se);
            return false;
        }
    }

    @Override
    public UserTransaction getUserTransaction() {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    @Override
    public void cleanup() {
        // nothing to clean
    }
}