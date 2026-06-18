package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.function.FailableFunction;
import org.d2ab.function.ObjIntFunction;
import org.d2ab.function.ObjIntPredicate;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;
import jakarta.xml.ws.Holder;

public class UpdateVersionTest {

	private static Method METHOD_AND, METHOD_GET_CLASS = null;

	@BeforeSuite
	void beforeSuite() throws NoSuchMethodException {
		//
		final Class<?> clz = UpdateVersion.class;
		//
		(METHOD_AND = clz.getDeclaredMethod("and", Boolean.TYPE, Boolean.TYPE, boolean[].class)).setAccessible(true);
		//
		(METHOD_GET_CLASS = clz.getDeclaredMethod("getClass", Object.class)).setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private Boolean contains, test, containsKey, hasNext, booleanValue;

		private Integer next;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			if (Objects.equals(getReturnType(method), Void.TYPE)) {
				//
				return null;
				//
			} // if
				//
			final String name = getName(method);
			//
			if (proxy instanceof Collection && Objects.equals(name, "contains")) {
				//
				return contains;
				//
			} else if (proxy instanceof Map) {
				//
				if (contains(Arrays.asList("put", "get"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "containsKey")) {
					//
					return containsKey;
					//
				} // if
					//
			} else if (proxy instanceof Path && Objects.equals(name, "toFile")) {
				//
				return null;
				//
			} else if (proxy instanceof Member && Objects.equals(name, "getName")) {
				//
				return null;
				//
			} else if (proxy instanceof Stream) {
				//
				if (Objects.equals(name, "toList")) {
					//
					return null;
					//
				} else if (Objects.equals(name, "filter")) {
					//
					return proxy;
					//
				} // if
					//
			} else if (proxy instanceof XMLStreamReader) {
				//
				if (Objects.equals(name, "hasNext")) {
					//
					return hasNext;
					//
				} else if (Objects.equals(name, "next")) {
					//
					return next;
					//
				} else if (contains(Arrays.asList("getLocation", "getLocalName"), name)) {
					//
					return null;
					//
				} // if
					//
			} else if (proxy instanceof Entry && contains(Arrays.asList("getKey", "getValue"), name)) {
				//
				return null;
				//
			} else if (Boolean.logicalOr(proxy instanceof Predicate, proxy instanceof ObjIntPredicate)
					&& Objects.equals(name, "test")) {
				//
				return test;
				//
			} else if (Boolean.logicalOr(proxy instanceof FailableFunction, proxy instanceof ObjIntFunction)
					&& Objects.equals(name, "apply")) {
				//
				return null;
				//
			} else if (isAssignableFrom(Class.forName("org.apache.commons.lang3.UpdateVersion$BooleanMap"),
					UpdateVersionTest.getClass(proxy)) && Objects.equals(name, "getBoolean")) {
				//
				return booleanValue;
				//
			} // if
				//
			throw new Throwable(name);
			//
		}

		private static boolean isAssignableFrom(final Class<?> a, final Class<?> b) {
			return a != null && b != null && a.isAssignableFrom(b);
		}

	}

	private static Class<?> getClass(final Object instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_CLASS != null ? METHOD_GET_CLASS.invoke(null, instance) : null;
			if (obj == null) {
				return null;
			} else if (obj instanceof Class<?>) {
				return (Class<?>) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static Class<?> getReturnType(final Method instance) {
		return instance != null ? instance.getReturnType() : null;
	}

	@Test
	void testNull() throws ClassNotFoundException {
		//
		final Method[] ms = UpdateVersion.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Object result = null;
		//
		String toString = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Collection<Object> collection = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null
					|| Boolean.logicalAnd(Objects.equals(getName(m), "and"), Arrays.equals(parameterTypes,
							new Class[] { Boolean.TYPE, Boolean.TYPE, boolean[].class }))) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if (Objects.equals(ArrayUtils.get(parameterTypes, j), Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else {
					//
					add(collection, null);
					//
				} // if
					//
			} // for
				//
			toString = Objects.toString(m);
			//
			result = Narcissus.invokeStaticMethod(m, toArray(collection));
			//
			if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))
					|| Boolean.logicalAnd(Objects.equals(getName(m), "execute"),
							Arrays.equals(parameterTypes,
									new Class<?>[] { XMLStreamReader.class, Map.class, Path.class, Iterable.class,
											Holder.class,
											Class.forName("org.apache.commons.lang3.UpdateVersion$BooleanMap") }))) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//
	}

	private static boolean contains(final Collection<?> items, final Object item) {
		return items != null && items.contains(item);
	}

	private static Object[] toArray(final Collection<?> instance) {
		return instance != null ? instance.toArray() : null;
	}

	@Test
	void testNotNull() throws ClassNotFoundException {
		//
		final Method[] ms = UpdateVersion.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Object result, name = null;
		//
		String toString = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		Collection<Object> collection = null;
		//
		final IH ih = new IH();
		//
		ih.contains = ih.test = ih.containsKey = ih.hasNext = ih.booleanValue = Boolean.FALSE;
		//
		ih.next = Integer.valueOf(0);
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null
					|| Boolean.logicalAnd(Objects.equals(name = getName(m), "toMap"),
							Arrays.equals(parameterTypes, new Class[] { String[].class }))
					|| Boolean.logicalAnd(Objects.equals(name, "and"), Arrays.equals(parameterTypes,
							new Class[] { Boolean.TYPE, Boolean.TYPE, boolean[].class }))) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if ((parameterType = ArrayUtils.get(parameterTypes, j)) != null && parameterType.isInterface()) {
					//
					add(collection, Reflection.newProxy(parameterType, ih));
					//
				} else if (Objects.equals(parameterType, String[].class)) {
					//
					add(collection, new String[] {});
					//
				} else if (Objects.equals(parameterType, XMLInputFactory.class)) {
					//
					add(collection, XMLInputFactory.newDefaultFactory());
					//
				} else if (Objects.equals(parameterType, InputStream.class)) {
					//
					add(collection, new ByteArrayInputStream(new byte[] {}));
					//
				} else if (Objects.equals(parameterType, Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (parameterType != null && parameterType.isArray()) {
					//
					add(collection, Array.newInstance(parameterType, 0));
					//
				} else {
					//
					add(collection, Narcissus.allocateInstance(parameterType));
					//
				} // if
					//
			} // for
				//
			toString = Objects.toString(m);
			//
			result = Narcissus.invokeStaticMethod(m, toArray(collection));
			//
			if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))
					|| Boolean.logicalAnd(Objects.equals(name = getName(m), "createXMLStreamReader"),
							Arrays.equals(parameterTypes, new Class<?>[] { XMLInputFactory.class, InputStream.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "getClass"),
							Arrays.equals(parameterTypes, new Class<?>[] { Object.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "filter"),
							Arrays.equals(parameterTypes, new Class<?>[] { Stream.class, Predicate.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "execute"),
							Arrays.equals(parameterTypes,
									new Class<?>[] { XMLStreamReader.class, Map.class, Path.class, Iterable.class,
											Holder.class,
											Class.forName("org.apache.commons.lang3.UpdateVersion$BooleanMap") }))) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static void clear(final Collection<?> instance) {
		if (instance != null) {
			instance.clear();
		}
	}

	@Test
	public void testMain() throws Exception {
		//
		UpdateVersion.main(new String[] { "=", "= ", " =", "== ", "file=" });
		//
		UpdateVersion.main(new String[] { "file=pom.xml" });
		//
		UpdateVersion.main(new String[] { "file=pom.xml", "groupId=org.testng", "artifactId=testng" });
		//
		final File file = File.createTempFile(RandomStringUtils.secure().nextAlphanumeric(3), ".xml");
		//
		if (file != null) {
			//
			file.deleteOnExit();
			//
		} // if
			//
		final Path path = file != null ? file.toPath() : null;
		//
		Files.writeString(path,
				"<project><dependencies><dependency><groupId>org.apache.commons</groupId><artifactId>commons-lang3</artifactId><version>3.19.0</version><scope></scope><exclusions></exclusions></dependency></dependencies></project>");
		//
		UpdateVersion.main(new String[] { "file=" + (file != null ? file.getAbsolutePath() : null),
				"groupId=org.apache.commons", "artifactId=commons-lang3", "version=3.20.0" });
		//
		Assert.assertEquals(Files.readString(path),
				"<project><dependencies><dependency><groupId>org.apache.commons</groupId><artifactId>commons-lang3</artifactId><version>3.20.0</version><scope></scope><exclusions></exclusions></dependency></dependencies></project>");
		//
		if (file != null) {
			//
			file.delete();
			//
		} // if
			//
	}

	@Test
	public void testAnd() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(METHOD_AND != null ? METHOD_AND.invoke(null, Boolean.TRUE, Boolean.TRUE, null) : null,
				Boolean.TRUE);
		//
		Assert.assertEquals(
				METHOD_AND != null ? METHOD_AND.invoke(null, Boolean.TRUE, Boolean.TRUE, new boolean[] { false })
						: null,
				Boolean.FALSE);
		//
	}

}