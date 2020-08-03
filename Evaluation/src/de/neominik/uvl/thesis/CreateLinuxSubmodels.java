package de.neominik.uvl.thesis;

import static de.neominik.uvl.thesis.Util.toLines;
import static de.neominik.uvl.thesis.Util.walk;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.init.FMCoreLibrary;
import de.ovgu.featureide.fm.core.init.LibraryManager;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.uvl.UVLFeatureModelFormat;

public class CreateLinuxSubmodels {

	/**
	 * Uses the Feature model of the Linux Kernel v2.6.33-rc3 and the corresponding
	 * Kconfig files to create submodels in UVL that have the same features and tree
	 * structure as the original Kconfig files. As these submodels are intended for
	 * editability metrics only, models that reference others in Kconfig won't do so
	 * in the generated UVL models. Instead, the import will be included as a
	 * regular feature.
	 */
	public static void main(String[] args) throws Exception {
		LibraryManager.registerLibrary(FMCoreLibrary.getInstance());

		IFeatureModel root = FeatureModelManager.load(Paths.get("linux/linux-2.6.33.3.xml"));

		walk("linux/Kconfig-2.6.33-rc3").forEach(p -> {

			IFeatureModel newSubModel = root.clone();
			Set<String> featureNames = extractFeatureNamesFromKconfig(p);

			removeOtherFeatures(newSubModel, featureNames);
			renameFeaturesStartingWithDigit(newSubModel);
			removeOtherConstraints(newSubModel, featureNames);

			saveSubmodel(p, newSubModel);
		});
	}

	private static Set<String> extractFeatureNamesFromKconfig(Path p) {
		Set<String> featureNames = toLines(p).stream().filter(s -> s.matches("(menu)?config.*"))
				.map(l -> l.substring(l.indexOf(" ") + 1)).collect(Collectors.toSet());
		return featureNames;
	}

	private static void removeOtherFeatures(IFeatureModel newSubModel, Set<String> featureNames) {
		newSubModel.getFeatures().stream().filter(f -> !featureNames.contains(f.getName()))
				.forEach(newSubModel::deleteFeature);
	}

	private static void renameFeaturesStartingWithDigit(IFeatureModel newSubModel) {
		newSubModel.getFeatures().stream().filter(f -> f.getName().matches("\\d.*"))
				.forEach(f -> f.setName("E" + f.getName()));
	}

	private static void removeOtherConstraints(IFeatureModel newSubModel, Set<String> featureNames) {
		Set<IConstraint> toRemove = newSubModel.getConstraints().stream().filter(c -> c != null)
				.filter(c -> !featureNames.containsAll(
						c.getContainedFeatures().stream().map(f -> f.getName()).collect(Collectors.toSet())))
				.collect(Collectors.toSet());
		toRemove.forEach(newSubModel::removeConstraint);
	}

	private static void saveSubmodel(Path path, IFeatureModel subModel) {
		Path folder = Paths.get("linux/submodels").resolve(path.subpath(2, path.getNameCount() - 1));
		Path uvlPath = folder.subpath(0, folder.getNameCount() - 1).resolve(folder.getFileName() + ".uvl");
		System.out.println("Creating " + uvlPath);
		try {
			uvlPath.getParent().toFile().mkdirs();
			uvlPath.toFile().createNewFile();
			FeatureModelManager.save(subModel, uvlPath, new UVLFeatureModelFormat());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
