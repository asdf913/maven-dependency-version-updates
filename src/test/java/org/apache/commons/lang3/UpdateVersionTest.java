package org.apache.commons.lang3;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

public class UpdateVersionTest {

	private static class IH implements InvocationHandler {

		private Boolean contains;

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
			} else if (proxy instanceof Map && Objects.equals(name, "put")) {
				//
				return null;
				//
			} // if
				//
			throw new Throwable(name);
			//
		}

	}

	private static Class<?> getReturnType(final Method instance) {
		return instance != null ? instance.getReturnType() : null;
	}

	@Test
	void testNull() {
		//
		final Method[] ms = UpdateVersion.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Object result = null;
		//
		String toString = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()) {
				//
				continue;
				//
			} // if
				//
			toString = Objects.toString(m);
			//
			result = Narcissus.invokeStaticMethod(m, toArray(Collections.nCopies(m.getParameterCount(), null)));
			//
			if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))) {
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
	void testNotNull() {
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
		Class<?> parameterType = null;
		//
		Collection<Object> collection = null;
		//
		final IH ih = new IH();
		//
		ih.contains = Boolean.FALSE;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null
					|| Boolean.logicalAnd(Objects.equals(getName(m), "toMap"),
							Arrays.equals(parameterTypes, new Class[] { String[].class }))) {
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
			if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))) {
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
	public void testMain() throws XMLStreamException, IOException, IllegalAccessException {
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

}