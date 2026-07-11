package vn.aequitas.coreplatform.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import vn.aequitas.coreplatform.application.common.exception.NotFoundException;
import vn.aequitas.coreplatform.domain.common.BaseEntity;
import vn.aequitas.coreplatform.domain.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic {@link Repository} implementation backed by Spring Data's
 * {@link SimpleJpaRepository} plus the shared {@link EntityManager}. Port of the
 * .NET generic {@code Repository<T>}.
 *
 * <p>Writes use {@code persist}/{@code merge} directly (the ids are
 * application-assigned, so {@code persist} inserts) and flush immediately,
 * mirroring the eager {@code SaveChangesAsync} calls of the .NET repository. The
 * enclosing {@code @Transactional} handler provides atomicity.</p>
 *
 * @param <T> entity type
 */
public class JpaRepositoryImpl<T extends BaseEntity> implements Repository<T> {

    private final EntityManager entityManager;
    private final SimpleJpaRepository<T, UUID> delegate;

    public JpaRepositoryImpl(Class<T> entityClass, EntityManager entityManager) {
        this.entityManager = entityManager;
        this.delegate = new SimpleJpaRepository<>(entityClass, entityManager);
    }

    @Override
    public List<T> getAll() {
        return delegate.findAll();
    }

    @Override
    public T getById(UUID id) {
        return delegate.findById(id)
                .orElseThrow(() -> new NotFoundException("Entity with ID " + id + " not found."));
    }

    @Override
    public Optional<T> getOne(Specification<T> specification) {
        return delegate.findOne(specification);
    }

    @Override
    public List<T> getListByCondition(Specification<T> specification) {
        return delegate.findAll(specification);
    }

    @Override
    public List<T> getListByCondition(Specification<T> specification, Sort sort) {
        return delegate.findAll(specification, sort);
    }

    @Override
    public long count(Specification<T> specification) {
        return delegate.count(specification);
    }

    @Override
    public List<T> getPaged(Specification<T> specification, Sort sort, int page, int pageSize) {
        return delegate.findAll(specification, PageRequest.of(page - 1, pageSize, sort)).getContent();
    }

    @Override
    public List<T> getListByIds(Collection<UUID> ids) {
        return delegate.findAllById(ids);
    }

    @Override
    public void add(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }

    @Override
    public void addRange(Iterable<T> entities) {
        for (T entity : entities) {
            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

    @Override
    public void updateRange(Iterable<T> entities) {
        for (T entity : entities) {
            entityManager.merge(entity);
        }
        entityManager.flush();
    }

    @Override
    public void deleteById(UUID id) {
        T entity = getById(id);
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
        entityManager.flush();
    }

    @Override
    public void deleteByIds(Collection<UUID> ids) {
        List<T> entities = delegate.findAllById(ids);
        for (T entity : entities) {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
        }
        entityManager.flush();
    }
}
