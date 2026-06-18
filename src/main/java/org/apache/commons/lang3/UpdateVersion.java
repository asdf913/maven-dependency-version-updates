package org.apache.commons.lang3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.d2ab.function.ObjIntFunction;
import org.d2ab.function.ObjIntPredicate;

import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;
import jakarta.xml.ws.Holder;

public class UpdateVersion {

	private static class Dependency {

		private String groupId, artifactId, version;

		private Integer versionIndexStart, versionIndexEnd;

	}

	private static class IH implements InvocationHandler {

		private Map<Object, Object> map = null;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			final String name = getName(method);
			//
			if (proxy instanceof BooleanMap) {
				//
				if (Objects.equals(name, "getBoolean") && args != null && args.length > 0) {
					//
					final Object arg = ArrayUtils.get(args, 0);
					//
					if (!containsKey(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), arg)) {
						//
						throw new IllegalStateException(Objects.toString(arg));
						//
					} // if
						//
					return get(map, arg);
					//
				} else if (Objects.equals(name, "setBoolean") && args != null && args.length > 1) {
					//
					put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), ArrayUtils.get(args, 0),
							ArrayUtils.get(args, 1));
					//
					return null;
					//
				} // if
					//
			} // if
				//
			throw new Throwable(name);
			//
		}
	}

	public static void main(final String[] args) throws Exception {
		//
		final Map<String, String> map = toMap(args);
		//
		final String file = get(map, "file");
		//
		final Path path = testAndApply(Objects::nonNull, file, Path::of, null);
		//
		final Iterable<String> lines = testAndApply(x -> isFile(toFile(x)), path, Files::readAllLines, null);
		//
		final XMLStreamReader xmlStreamReader = testAndApply(x -> isFile(toFile(x)), path,
				x -> createXMLStreamReader(XMLInputFactory.newInstance(), Files.newInputStream(x)), null);
		//
		Dependency dependency = null;
		//
		Entry<BreakOrContinue, Dependency> entry = null;
		//
		IH ih = null;
		//
		BooleanMap booleanMap = null;
		//
		while (hasNext(xmlStreamReader)) {
			//
			if (booleanMap == null) {
				//
				BooleanMap.setBoolean(
						booleanMap = Reflection.newProxy(BooleanMap.class, ih = ObjectUtils.getIfNull(ih, IH::new)),
						"dependencies", false);
				//
				BooleanMap.setBoolean(booleanMap, "exclusions", false);
				//
			} // if
				//
			if (Objects.equals(
					getKey(entry = execute(xmlStreamReader, map, path, lines, new Holder<>(dependency), booleanMap)),
					BreakOrContinue.Continue)) {
				//
				dependency = getValue(entry);
				//
				continue;
				//
			} else if (Objects.equals(entry != null ? entry.getKey() : null, BreakOrContinue.Break)) {
				//
				break;
				//
			} else {
				//
				dependency = getValue(entry);
				//
			} // if
				//
		} // while
			//
		close(xmlStreamReader);
		//
	}

	private static <K> K getKey(final Entry<K, ?> instance) {
		return instance != null ? instance.getKey() : null;
	}

	private static <V> V getValue(final Entry<?, V> instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static enum BreakOrContinue {
		Break, Continue;
	}

	private static interface BooleanMap {

		boolean getBoolean(final String key);

		void setBoolean(final String key, final boolean value);

		static boolean getBoolean(final BooleanMap instance, final String key) {
			return instance != null && instance.getBoolean(key);
		}

		static void setBoolean(final BooleanMap instance, final String key, final boolean value) {
			if (instance != null) {
				instance.setBoolean(key, value);
			}
		}

	}

	private static Entry<BreakOrContinue, Dependency> execute(final XMLStreamReader xmlStreamReader,
			final Map<String, String> map, final Path path, final Iterable<String> lines,
			final Holder<Dependency> holder, final BooleanMap booleanMap)
			throws XMLStreamException, IOException, IllegalAccessException {
		//
		String localName = null;
		//
		final boolean dependencies = BooleanMap.getBoolean(booleanMap, "dependencies");
		//
		final boolean exclusions = BooleanMap.getBoolean(booleanMap, "exclusions");
		//
		final int event = next(xmlStreamReader);
		//
		String line = null;
		//
		Location location = null;
		//
		Dependency dependency = value(holder);
		//
		if (event == XMLStreamConstants.START_ELEMENT) {
			//
			if (Boolean.logicalAnd(Objects.equals(localName = getLocalName(xmlStreamReader), "dependencies"),
					!dependencies)) {
				//
				BooleanMap.setBoolean(booleanMap, "dependencies", true);
				//
				return Pair.of(BreakOrContinue.Continue, dependency);
				//
			} else if (Boolean.logicalAnd(Objects.equals(localName, "exclusions"), !exclusions)) {
				//
				BooleanMap.setBoolean(booleanMap, "exclusions", true);
				//
				return Pair.of(BreakOrContinue.Continue, dependency);
				//
			} else if (!dependencies || contains(Arrays.asList("dependency", "scope"), localName) || exclusions
					|| getLocation(xmlStreamReader) == null) {
				//
				return Pair.of(BreakOrContinue.Continue, dependency);
				//
			} // if
				//
		} else if (event == XMLStreamConstants.END_ELEMENT) {
			//
			location = getLocation(xmlStreamReader);
			//
			if (Boolean.logicalAnd(Objects.equals(localName = getLocalName(xmlStreamReader), "dependencies"),
					!dependencies)) {
				//
				BooleanMap.setBoolean(booleanMap, "dependencies", false);
				//
				return Pair.of(BreakOrContinue.Break, dependency);
				//
			} else if (Boolean.logicalOr(Objects.equals(localName, "scope"),
					Boolean.logicalAnd(Objects.equals(localName, "exclusions"), !exclusions))) {
				//
				BooleanMap.setBoolean(booleanMap, "exclusions", false);
				//
				return Pair.of(BreakOrContinue.Continue, dependency);
				//
			} else if (Objects.equals(localName, "dependency")) {
				//
				updateVersion(dependency, map, path);
				//
				return Pair.of(BreakOrContinue.Continue, null);
				//
			} else if (and(contains(Arrays.asList("groupId", "artifactId", "version"), localName), dependencies,
					!exclusions)) {
				//
				FieldUtils
						.writeDeclaredField(dependency = ObjectUtils.getIfNull(dependency, Dependency::new), localName,
								StringUtils.substringBetween(IterableUtils.get(lines, location.getLineNumber() - 1),
										StringUtils.join("<", localName, ">"), StringUtils.join("</", localName, ">")),
								true);
				//
			} // if
				//
			if (and(Objects.equals(localName, "version"), dependencies, !exclusions)
					&& (dependency = ObjectUtils.getIfNull(dependency, Dependency::new)) != null
					&& (line = IterableUtils.get(lines, location.getLineNumber() - 1)) != null) {
				//
				dependency.versionIndexStart = testAndApply((a, b) -> b == lastIndexOf(a, "<version>"), line,
						line.indexOf("<version>"), (a, b) -> Integer.valueOf(b), null);
				//
				dependency.versionIndexEnd = testAndApply((a, b) -> b == lastIndexOf(a, "</version>"), line,
						line.indexOf("</version>"), (a, b) -> Integer.valueOf(b), null);
				//
			} // if
				//
		} // if
			//
		return Pair.of(null, dependency);
		//
	}

	private static <T> T value(final Holder<T> instance) {
		return instance != null ? instance.value : null;
	}

	private static Location getLocation(final XMLStreamReader instance) {
		return instance != null ? instance.getLocation() : null;
	}

	private static String getLocalName(final XMLStreamReader instance) {
		return instance != null ? instance.getLocalName() : null;
	}

	private static int next(final XMLStreamReader instance) throws XMLStreamException {
		return instance != null ? instance.next() : 0;
	}

	private static boolean hasNext(final XMLStreamReader instance) throws XMLStreamException {
		return instance != null && instance.hasNext();
	}

	private static void updateVersion(final Dependency dependency, final Map<String, String> map, final Path path)
			throws IOException {
		//
		if (dependency != null) {
			//
			String version = null;
			//
			if (Objects.equals(dependency.groupId, get(map, "groupId"))
					&& Objects.equals(dependency.artifactId, get(map, "artifactId")) && containsKey(map, "version")
					&& !Objects.equals(version = get(map, "version"), dependency.version)) {
				//
				final StringBuilder sb = new StringBuilder(
						ObjectUtils.getIfNull(testAndApply(x -> isFile(toFile(x)), path, Files::readString, null), ""));
				//
				if (dependency.versionIndexStart != null && dependency.versionIndexEnd != null) {
					//
					sb.delete(dependency.versionIndexStart, dependency.versionIndexEnd);
					//
				} // if
					//
				System.out.println(String.format("groupId=%1$s,artifactId=%2$s,version=[%3$s->%4$s]",
						dependency.groupId, dependency.artifactId, dependency.version, version));
				//
				Files.writeString(path,
						dependency.versionIndexStart != null
								? sb.insert(dependency.versionIndexStart, String.join("", "<version>", version))
								: sb);
				//
			} // if
				//
		} // if
			//
	}

	private static boolean and(final boolean a, final boolean b, final boolean... bs) {
		//
		boolean result = a && b;
		//
		if (!result) {
			//
			return result;
			//
		} // if
			//
		for (int i = 0; bs != null && i < bs.length; i++) {
			//
			if (!(result &= bs[i])) {
				//
				return result;
				//
			} // if
				//
		} // for
			//
		return result;
		//
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static int lastIndexOf(final String a, final String b) {
		//
		if (a == null) {
			//
			return -1;
			//
		} // if
			//
		final Field value = testAndApply(x -> IterableUtils.size(x) == 1, toList(
				filter(FieldUtils.getAllFieldsList(getClass(a)).stream(), f -> Objects.equals(getName(f), "value"))),
				x -> IterableUtils.get(x, 0), null);
		//
		return value == null || Narcissus.getField(a, value) != null ? a.lastIndexOf(b) : -1;
		//
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static <T> List<T> toList(final Stream<T> instance) {
		return instance != null ? instance.toList() : null;
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		return instance != null ? instance.filter(predicate) : instance;
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static <T, R> R testAndApply(final ObjIntPredicate<T> predicate, final T object, final int integer,
			final ObjIntFunction<T, R> functionTrue, final ObjIntFunction<T, R> functionFalse) {
		return predicate != null && predicate.test(object, integer) ? apply(functionTrue, object, integer)
				: apply(functionFalse, object, integer);
	}

	private static <T, R> R apply(final ObjIntFunction<T, R> instance, final T object, final int integer) {
		return instance != null ? instance.apply(object, integer) : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static File toFile(final Path instance) {
		return instance != null ? instance.toFile() : null;
	}

	private static XMLStreamReader createXMLStreamReader(final XMLInputFactory instance, final InputStream inputStream)
			throws XMLStreamException {
		return instance != null ? instance.createXMLStreamReader(inputStream) : null;
	}

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
	}

	private static void close(final XMLStreamReader instance) throws XMLStreamException {
		if (instance != null) {
			instance.close();
		}
	}

	private static boolean contains(final Collection<?> instance, final Object item) {
		return instance != null && instance.contains(item);
	}

	private static boolean isFile(final File instance) {
		return instance != null && instance.getPath() != null && instance.isFile();
	}

	private static Map<String, String> toMap(final String... ss) {
		//
		String s = null;
		//
		Map<String, String> map = null;
		//
		for (int i = 0; i < length(ss); i++) {
			//
			if (Objects.equals(s = ArrayUtils.get(ss, i), "=")) {
				//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), "", "");
				//
			} else if (s != null && s.length() == 2 && s.charAt(0) == '=') {
				//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), "", s.substring(1, s.length()));
				//
			} else if (s != null && s.length() == 2 && s.charAt(s.length() - 1) == '=') {
				//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), s.substring(0, s.length() - 1), "");
				//
			} else if (s != null && s.indexOf('=') >= 0 && s.indexOf('=') == s.lastIndexOf('=')) {
				//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), StringUtils.substringBefore(s, '='),
						StringUtils.substringAfter(s, '='));
				//
			} else if (s != null && s.length() > 2 && s.indexOf('=') != s.lastIndexOf('=')) {
				//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), StringUtils.substring(s, 0, s.indexOf('=')),
						StringUtils.substring(s, s.indexOf('=') + 1));
				//
			} // if
				//
		} // for
			//
		return map;
		//
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static int length(final Object[] instance) {
		return instance != null ? instance.length : 0;
	}

}