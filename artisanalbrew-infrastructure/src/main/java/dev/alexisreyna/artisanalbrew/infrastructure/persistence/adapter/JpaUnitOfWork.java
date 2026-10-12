package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.UnitOfWork;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link UnitOfWork} over Spring transactions (mirrors {@code UnitOfWork.cs}).
 * Serializable for payment-claim races, default isolation otherwise; closing
 * without commit rolls back.
 */
@Component
public class JpaUnitOfWork implements UnitOfWork {
    private final PlatformTransactionManager transactionManager;

    public JpaUnitOfWork(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public UnitOfWorkTransaction beginSerializableTransaction() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new TemplateTransaction(new TransactionTemplate(transactionManager, definition));
    }

    @Override
    public UnitOfWorkTransaction beginTransaction() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new TemplateTransaction(new TransactionTemplate(transactionManager, definition));
    }

    private static final class TemplateTransaction implements UnitOfWorkTransaction {
        private final TransactionTemplate template;
        private org.springframework.transaction.TransactionStatus status;
        private boolean committed;

        TemplateTransaction(TransactionTemplate template) {
            this.template = template;
            this.status = template.getTransactionManager().getTransaction(template);
        }

        @Override
        public void commit() {
            template.getTransactionManager().commit(status);
            committed = true;
        }

        @Override
        public void close() {
            if (!committed && !status.isCompleted()) {
                template.getTransactionManager().rollback(status);
            }
        }
    }
}
