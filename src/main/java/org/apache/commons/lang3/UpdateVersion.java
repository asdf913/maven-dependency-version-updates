package org.apache.commons.lang3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

public class UpdateVersion {

	private static class Dependency {

		private String groupId, artifactId, version;

		private Integer versionIndexStart, versionIndexEnd;

	}

	public static void main(final String[] args) throws XMLStreamException, IOException, IllegalAccessException {
		//
		final Map<String, String> map = toMap(args);
		//
		final String file = get(map, "file");
		//
		final Path path = file != null ? Path.of(file) : null;
		//
		final Iterable<String> lines = isFile(toFile(path)) ? Files.readAllLines(path) : null;
		//
		final XMLStreamReader xmlStreamReader = isFile(toFile(path))
				? createXMLStreamReader(XMLInputFactory.newInstance(), Files.newInputStream(path))
				: null;
		//
		String localName = null;
		//
		boolean dependencies = false, exclusions = false;
		//
		int event = 0;
		//
		Dependency dependency = null;
		//
		String line, version = null;
		//
		Location location = null;
		//
		int indexOf = 0;
		//
		while (xmlStreamReader != null && xmlStreamReader.hasNext()) {
			//
			if ((event = xmlStreamReader.next()) == XMLStreamConstants.START_ELEMENT) {
				//
				if ((Objects.equals(localName = xmlStreamReader.getLocalName(), "dependencies") && !dependencies
						&& (dependencies = true)) || !dependencies || Objects.equals(localName, "dependency")
						|| Objects.equals(localName, "scope")
						|| (Objects.equals(localName, "exclusions") && !exclusions && (exclusions = true)) || exclusions
						|| (location = xmlStreamReader.getLocation()) == null) {
					//
					continue;
					//
				} // if
					//
			} else if (event == XMLStreamConstants.END_ELEMENT) {
				//
				location = xmlStreamReader.getLocation();
				//
				if ((Objects.equals(localName = xmlStreamReader.getLocalName(), "dependencies") && dependencies
						&& !(dependencies = false))) {
					//
					break;
					//
				} else if (Objects.equals(localName, "scope")
						|| (Objects.equals(localName, "exclusions") && exclusions && !(exclusions = false))) {
					//
					continue;
					//
				} else if (Objects.equals(localName, "dependency")) {
					//
					if (dependency != null) {
						//
						if (map != null && Objects.equals(dependency.groupId, get(map, "groupId"))
								&& Objects.equals(dependency.artifactId, get(map, "artifactId"))) {
							//
							if (map.containsKey("version")
									&& !Objects.equals(version = get(map, "version"), dependency.version)) {
								//
								final StringBuilder sb = new StringBuilder(ObjectUtils
										.getIfNull(isFile(toFile(path)) ? Files.readString(path) : null, ""));
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
								Files.writeString(path, dependency.versionIndexStart != null
										? sb.insert(dependency.versionIndexStart, String.join("", "<version>", version))
										: sb);
								//
							} // if
								//
						} // if
							//
						dependency = null;
						//
					} // if
						//
					continue;
					//
				} else if (contains(Arrays.asList("groupId", "artifactId", "version"), localName) && dependencies
						&& !exclusions && (dependency = ObjectUtils.getIfNull(dependency, Dependency::new)) != null) {
					//
					FieldUtils.writeDeclaredField(dependency, localName,
							StringUtils.substringBetween(IterableUtils.get(lines, location.getLineNumber() - 1),
									StringUtils.join("<", localName, ">"), StringUtils.join("</", localName, ">")),
							true);
					//
				} // if
					//
				if (Objects.equals(localName, "version") && dependencies && !exclusions
						&& (dependency = ObjectUtils.getIfNull(dependency, Dependency::new)) != null) {
					//
					if ((line = IterableUtils.get(lines, location.getLineNumber() - 1)) != null) {
						//
						dependency.versionIndexStart = (indexOf = line.indexOf("<version>")) >= 0
								&& line.indexOf("<version>") == line.lastIndexOf("<version>") ? Integer.valueOf(indexOf)
										: null;
						//
						dependency.versionIndexEnd = (indexOf = line.indexOf("</version>")) >= 0
								&& line.indexOf("</version>") == line.lastIndexOf("</version>")
										? Integer.valueOf(indexOf)
										: null;
						//
					} // if
						//
				} // if
					//
			} // if
				//
		} // while
			//
		close(xmlStreamReader);
		//
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