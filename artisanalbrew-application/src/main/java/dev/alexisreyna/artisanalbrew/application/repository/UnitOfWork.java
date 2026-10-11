package dev.alexisreyna.artisanalbrew.application.repository;

/**
 * Port of {@code Repositories/IUnitOfWork.cs}.
 * Maps to Spring's {@code @Transactional}: serializable for payment-claim races,
 * default isolation otherwise. Disposing without commit rolls back.
 */
public interface UnitOfWork {
    UnitOfWorkTransaction beginSerializableTransaction();

    UnitOfWorkTransaction beginTransaction();

    interface UnitOfWorkTransaction extends AutoCloseable {
        void commit();

        @Override
        void close();
    }
}
