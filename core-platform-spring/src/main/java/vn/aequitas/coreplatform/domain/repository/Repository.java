package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic persistence port, the Spring/JPA counterpart of the .NET
 * {@code IRepository&lt;T&gt;}. Predicate-based queries take a Spring Data
 * {@link Specification} in place of the .NET {@code Expression&lt;Func&lt;T,bool&gt;&gt;}.
 *
 * @param <T> aggregate / entity type
 */
public interface Repository<T> {

    /** All rows (equivalent of {@code GetAll()} / {@code DbSet.AsNoTracking()}). */
    List<T> getAll();

    /** Find by id, throwing {@code NotFoundException} when absent (mirrors {@code GetByIdAsync}). */
    T getById(UUID id);

    /** First row matching the specification, if any (mirrors {@code GetOne}). */
    Optional<T> getOne(Specification<T> specification);

    /** All rows matching the specification (mirrors {@code GetListByConditionAsync}). */
    List<T> getListByCondition(Specification<T> specification);

    /** All rows matching the specification, ordered. */
    List<T> getListByCondition(Specification<T> specification, Sort sort);

    /** Count of rows matching the specification. */
    long count(Specification<T> specification);

    /** A single page (1-based) of rows matching the specification, ordered. */
    List<T> getPaged(Specification<T> specification, Sort sort, int page, int pageSize);

    /** Rows whose id is in the supplied set (mirrors {@code GetListByIdAsync}). */
    List<T> getListByIds(Collection<UUID> ids);

    void add(T entity);

    void addRange(Iterable<T> entities);

    void update(T entity);

    void updateRange(Iterable<T> entities);

    void deleteById(UUID id);

    void deleteByIds(Collection<UUID> ids);
}
