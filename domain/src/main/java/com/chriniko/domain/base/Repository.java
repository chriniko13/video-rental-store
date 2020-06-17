package com.chriniko.domain.base;

import java.util.List;

public interface Repository<E extends Record<I>, I> {

	E find(I id);

	I save(E entity);

	boolean delete(I id);

	void save(Iterable<E> entities);

	long count();

	int getTotalPages(int pageSize);

	List<E> get(int page, int pageSize);

	TxBoundary getTxBoundary();

}
