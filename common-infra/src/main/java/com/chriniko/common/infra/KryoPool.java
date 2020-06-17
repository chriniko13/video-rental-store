package com.chriniko.common.infra;

import com.esotericsoftware.kryo.Kryo;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.function.Consumer;
import java.util.function.Function;

public class KryoPool {

	private static GenericObjectPool<Kryo> pool;

	static {
		pool = new GenericObjectPool<>(new KryoObjectFactory());
		pool.setMaxTotal(12);
		pool.setBlockWhenExhausted(true);
		pool.setMaxWaitMillis(-1);
	}

	private KryoPool() {
	}

	static void use(Consumer<Kryo> c) {
		Kryo k = null;
		try {
			k = pool.borrowObject();
			c.accept(k);
		} catch (Exception e) {
			throw new InfrastructureException(e);
		} finally {
			try {
				if (null != k) {
					pool.returnObject(k);
				}
			} catch (Exception e) {
				// ignored
			}
		}
	}

	static <R> R use(Function<Kryo, R> f) {
		Kryo k = null;
		try {
			k = pool.borrowObject();
			return f.apply(k);
		} catch (Exception e) {
			throw new InfrastructureException(e);
		} finally {
			try {
				if (null != k) {
					pool.returnObject(k);
				}
			} catch (Exception e) {
				// ignored
			}
		}
	}

	public static class KryoObjectFactory extends BasePooledObjectFactory<Kryo> {

		@Override public Kryo create() throws Exception {
			return new Kryo();
		}

		@Override public PooledObject<Kryo> wrap(Kryo kryo) {
			return new DefaultPooledObject<>(kryo);
		}

		@Override public void passivateObject(PooledObject<Kryo> p) throws Exception {
			// nothing
		}

	}
}
