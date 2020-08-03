package de.neominik.uvl.thesis;

import static de.neominik.uvl.thesis.Util.toLines;
import static de.neominik.uvl.thesis.Util.walk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.ovgu.featureide.fm.core.init.FMCoreLibrary;
import de.ovgu.featureide.fm.core.init.LibraryManager;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;

public class Analyze {

	private static final String OUTPUT_VALUE_DELIMITER = ", ";

	/**
	 * Analyze number of features, maximum sibling distance, and maximum indentation
	 * level for automotive and linux submodels and write the results to files in
	 * {@code output/}
	 */
	public static void main(String[] args) throws Exception {
		LibraryManager.registerLibrary(FMCoreLibrary.getInstance());

		Stream.of("automotive", "linux").forEach(folder -> {
			streamOfMapsToMapOfStreams(walk(folder + "/submodels").map(analyze()))
					.forEach((type, values) -> saveResult(folder, type, values));
		});
	}

	private static Function<Path, Map<String, Integer>> analyze() {
		return path -> {
			int size = computeNumberOfFeatures(path);
			List<String> lines = toLines(path);
			int maxIndetation = computeMaxIndentationLevel(lines);
			int maxSiblingDistance = computeMaxSiblingDistance(lines, maxIndetation);

			return Map.of("numberOfFeatures", size, "maxSiblingDistance", maxSiblingDistance, "maxIndentationLevel",
					maxIndetation);
		};
	}

	private static int computeNumberOfFeatures(Path path) {
		return FeatureModelManager.load(path).getNumberOfFeatures();
	}

	private static int computeMaxIndentationLevel(List<String> lines) {
		return lines.stream().mapToInt(indentationLevel()).max().orElse(0);
	}

	private static int computeMaxSiblingDistance(List<String> lines, int maxIndetation) {
		return IntStream.range(3, maxIndetation) // start at level 3, the indentation of the root feature's children
				.map(level -> computeMaxSiblingDistanceForLevel(level, getRelevantLinesForLevel(lines, level))).max()
				.orElse(0);
	}

	private static int computeMaxSiblingDistanceForLevel(int i, Stream<Integer> lines) {
		return lines.collect(() -> new ArrayList<>(List.of(0)), countLinesBetweenSiblings(i), (a, b) -> {
		}).stream().mapToInt(Integer::intValue).max().orElse(0);
	}

	/**
	 * If the current line is indented further than the considered level i, increase
	 * count for current sibling at the end of the list. Otherwise start counting
	 * for the next sibling.
	 */
	private static BiConsumer<List<Integer>, ? super Integer> countLinesBetweenSiblings(int i) {
		return (acc, l) -> {
			if (l > i) {
				acc.set(acc.size() - 1, acc.get(acc.size() - 1) + 1);
			} else {
				acc.add(0);
			}
		};
	}

	/**
	 * Removes constraints, empty lines, and trailing features after the last
	 * sibling on level i.
	 */
	private static Stream<Integer> getRelevantLinesForLevel(List<String> lines, int i) {
		return lines.stream().filter(s -> !s.isEmpty()).collect(reverse()).mapToInt(indentationLevel())
				.dropWhile(l -> l != i).boxed().collect(reverse());
	}

	private static void saveResult(String folder, String type, Stream<Integer> values) {
		String text = values.sorted().map(Object::toString).collect(Collectors.joining(OUTPUT_VALUE_DELIMITER));
		Path p = Paths.get("output", folder + "_" + type);
		p.getParent().toFile().mkdirs();
		try {
			Files.writeString(p, text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determine indentation level for line. Assumes files to be indented with tabs.
	 */
	private static ToIntFunction<String> indentationLevel() {
		return (s) -> s.length() - s.stripLeading().length();
	}

	public static <T> Collector<T, ?, Stream<T>> reverse() {
		return Collectors.collectingAndThen(Collectors.toList(), list -> {
			Collections.reverse(list);
			return list.stream();
		});
	}

	private static <K, V> Map<K, Stream<V>> streamOfMapsToMapOfStreams(Stream<Map<K, V>> stream) {
		return stream.flatMap(m -> m.entrySet().stream()).collect(
				Collectors.toMap(e -> e.getKey(), e -> Stream.of(e.getValue()), (a, b) -> Stream.concat(a, b)));
	}
}
