package com.chriniko.domain.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseRepositoryTest {

	@Spy
	private BaseRepository<Test, Long> repository = new BaseRepository<BaseRepositoryTest.Test, Long>() {
		@Override protected Class<BaseRepositoryTest.Test> getEntityClass() {
			return BaseRepositoryTest.Test.class;
		}
	};

	@org.junit.Test
	public void getTotalPages() {

		// given
		int pageSize = 10;
		Mockito.doReturn(123L).when(repository).count();

		// when
		int totalPages = repository.getTotalPages(pageSize);

		// then
		Assert.assertEquals(13, totalPages);

	}

	// --- infra ---

	@Getter
	@RequiredArgsConstructor
	private static class Test implements Record<Long> {

		private final long id;

		@Override public Long extractId() {
			return id;
		}
	}
}
