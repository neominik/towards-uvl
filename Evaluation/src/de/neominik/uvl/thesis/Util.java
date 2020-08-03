package de.neominik.uvl.thesis;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Util {

	public static Stream<Path> walk(String path) {
		try {
			return Files.walk(Paths.get(path)).parallel().filter(Files::isRegularFile);
		} catch (IOException e) {
			return Stream.of();
		}
	}

	static List<String> toLines(Path p) {
		try {
			return Files.readAllLines(p);
		} catch (IOException e) {
		}
		return List.of();
	}

}
