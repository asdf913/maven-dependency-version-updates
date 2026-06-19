package org.apache.commons.lang3;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.github.toolfactory.narcissus.Narcissus;

public class UpdateVersion {

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Note {
		String value();
	}

	private static class Dependency {

		@Note("Group ID")
		private String groupId;

		@Note("Artifact ID")
		private String artifactId;

	}

	public static void main(final String[] args) throws Exception {
		//
		final Map<String, String> map = toMap(args);
		//
		final String file = get(map, "file");
		//
		final Path path = testAndApply(Objects::nonNull, file, Path::of, null);
		//
		if (!isFile(toFile(path))) {
			//
			System.out.println("Please select a file");
			//
			return;
			//
		} // if
			//
		try {
			//
			final XPath xp = newXPath(XPathFactory.newDefaultInstance());
			//
			final NodeList nodeList = cast(NodeList.class, evaluate(xp,
					"/*[local-name()=\"project\"]/*[local-name()=\"dependencies\"]/*[local-name()=\"dependency\"]",
					parse(newDocumentBuilder(DocumentBuilderFactory.newDefaultInstance()), toFile(path)),
					XPathConstants.NODESET));
			//
			Dependency dependency = null;
			//
			Node node = null;
			//
			Collection<Dependency> dependencies = null;
			//
			for (int i = 0; i < getLength(nodeList); i++) {
				//
				(dependency = new Dependency()).groupId = Objects.toString(
						evaluate(xp, "*[local-name()=\"groupId\"]", node = item(nodeList, i), XPathConstants.STRING));
				//
				dependency.artifactId = Objects
						.toString(evaluate(xp, "*[local-name()=\"artifactId\"]", node, XPathConstants.STRING));
				//
				add(dependencies = ObjectUtils.getIfNull(dependencies, ArrayList::new), dependency);
				//
			} // for
				//
			final String groupId = get(map, "groupId");
			//
			final String artifactId = get(map, "artifactId");
			//
			if (IterableUtils.isEmpty(dependencies = toList(filter(stream(dependencies), x -> x != null
					&& Objects.equals(x.groupId, groupId) && Objects.equals(x.artifactId, artifactId))))) {
				//
				System.out.println("No dependency found");
				//
			} else if (IterableUtils.size(dependencies) > 1) {
				//
				System.out.println("More than one dependency definition found");
				//
			} else {
				//
				final String string = Files.readString(path);
				//
				final int index1 = indexOf(string, String.format("<%1$s>%2$s</%1$s>", "groupId", groupId));
				//
				final int index2 = indexOf(string, String.format("<%1$s>%2$s</%1$s>", "artifactId", artifactId));
				//
				final int index3 = indexOf(string, "<version>", Math.max(index1, index2)) + 9;
				//
				final int index4 = indexOf(string, "</version>", Math.max(index1, index2));
				//
				final StringBuilder sb = new StringBuilder(ObjectUtils.getIfNull(string, ""));
				//
				sb.delete(index3, index4);
				//
				Files.writeString(path, sb.insert(index3, get(map, "version")));
				//
			} // if
				//
		} catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
	}

	private static int indexOf(final String a, final String b, final int fromIndex) {
		//
		if (a == null) {
			//
			return -1;
			//
		} // if
			//
		final Field value = testAndApply(x -> IterableUtils.size(x) == 1, toList(
				filter(stream(FieldUtils.getAllFieldsList(getClass(a))), f -> Objects.equals(getName(f), "value"))),
				x -> IterableUtils.get(x, 0), null);
		//
		return value == null || Narcissus.getField(a, value) != null ? a.indexOf(b, fromIndex) : -1;
		//
	}

	private static int indexOf(final String a, final String b) {
		//
		if (a == null) {
			//
			return -1;
			//
		} // if
			//
		final Field value = testAndApply(x -> IterableUtils.size(x) == 1, toList(
				filter(stream(FieldUtils.getAllFieldsList(getClass(a))), f -> Objects.equals(getName(f), "value"))),
				x -> IterableUtils.get(x, 0), null);
		//
		return value == null || Narcissus.getField(a, value) != null ? a.indexOf(b) : -1;
		//
	}

	private static <T> Stream<T> stream(final Collection<T> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static Node item(final NodeList instance, final int index) {
		return instance != null ? instance.item(index) : null;
	}

	private static int getLength(final NodeList instance) {
		return instance != null ? instance.getLength() : 0;
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static Object evaluate(final XPath instance, final String expression, final Object item,
			final QName returnType) throws XPathExpressionException {
		return instance != null ? instance.evaluate(expression, item, returnType) : null;
	}

	private static Document parse(final DocumentBuilder instance, final File file) throws SAXException, IOException {
		return instance != null && file != null && file.getPath() != null ? instance.parse(file) : null;
	}

	private static DocumentBuilder newDocumentBuilder(final DocumentBuilderFactory instance)
			throws ParserConfigurationException {
		return instance != null ? instance.newDocumentBuilder() : null;
	}

	private static XPath newXPath(final XPathFactory instance) {
		return instance != null ? instance.newXPath() : null;
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

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
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