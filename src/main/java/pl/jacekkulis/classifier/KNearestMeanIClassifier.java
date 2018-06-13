package pl.jacekkulis.classifier;

import Jama.Matrix;
import pl.jacekkulis.exception.EmptyModException;
import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleWithClass;
import pl.jacekkulis.utils.ClassStatisticData;
import pl.jacekkulis.utils.Common;
import pl.jacekkulis.utils.IDistanceCalculator;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class KNearestMeanIClassifier implements IClassifier {

	private static final double MAXIMAL_ACCEPTABLE_MOD_CENTER_DISLOCATION= 0.000001;
	private static final double H = 0.1;
	
	private pl.jacekkulis.utils.IDistanceCalculator IDistanceCalculator;
	private Map<ModelClass, ClassData> classesStatisticData;
	
	public KNearestMeanIClassifier(IDistanceCalculator IDistanceCalculator) {
		this.IDistanceCalculator = IDistanceCalculator;
	}

	@Override
	public void train(List<SampleWithClass> trainSamples) {
		classesStatisticData = calculateStatisticDataForClasses(trainSamples);
		
		System.out.println("Number of mods: ");
		for (ClassData classData : classesStatisticData.values()) {
			System.out.println(classData.classStatisticData.size());
		}
	}

	private Map<ModelClass, ClassData> calculateStatisticDataForClasses(
			List<SampleWithClass> trainSamples) {
		Map<ModelClass, List<SampleWithClass>> samplesGroupedByClass = groupSamplesByClass(trainSamples);
		
		Map<ModelClass, ClassData> classesStatisticData = new HashMap<>();
		for (ModelClass modelClass : samplesGroupedByClass.keySet()) {
			ClassData classData = calculateStatisticDataForClass(samplesGroupedByClass.get(modelClass), modelClass);
			classesStatisticData.put(modelClass, classData);
		}
		
		return classesStatisticData;
	}

	private Map<ModelClass, List<SampleWithClass>> groupSamplesByClass(
			List<SampleWithClass> trainSamples) {
		return trainSamples
				.stream()
				.collect(Collectors.groupingBy(SampleWithClass::getModelClass));
	}
	
	private ClassData calculateStatisticDataForClass(List<SampleWithClass> samplesOfClass, ModelClass modelClass) {
		List<Matrix> modCenters = findModCenters(samplesOfClass);	
		Map<Matrix, Matrix> covarianceMatricesForMods = calculateCovarianceMatricesForMods(samplesOfClass, modCenters);
		List<ClassStatisticData> classStatisticData = transformToList(covarianceMatricesForMods);
		
		return new ClassData(classStatisticData);
	}

	private List<Matrix> findModCenters(List<SampleWithClass> samples) {
		List<Double> errors = new ArrayList<>();
		Map<Integer, List<Matrix>> modCentersByNumberOfMods = new HashMap<>();
		int numberOfMods = 0;
		
		do {
			try {
				numberOfMods += 1;
				
				List<Matrix> modCenters = findModCentersForNMods(samples, numberOfMods);
				modCentersByNumberOfMods.put(numberOfMods, modCenters);
				errors.add(calculateError(modCenters, samples));
			} catch (EmptyModException e) {
				break;
			}
		} while (numberOfMods < 2 || errorDecreasedSignificantly(errors, numberOfMods));
		
		return modCentersByNumberOfMods.get(numberOfMods - 1);
	}

	private List<Matrix> findModCentersForNMods(List<? extends Sample> samples, int numberOfMods){
		if (numberOfMods == 1) {
			return Arrays.asList(Common.calculateMean(samples));
		} else {
			List<Matrix> modCenters = randomModCenters(samples, numberOfMods);
			
			double maxCenterDislocation;
			do {
				Map<Matrix, List<Sample>> samplesGroupedByMods = assignSamplesToMods(samples, modCenters);
				
				List<Matrix> newModCenters = correctModCenters(modCenters, samplesGroupedByMods);
				maxCenterDislocation = calculateMaxCenterDislocation(modCenters, newModCenters);
				
				modCenters = newModCenters;
			} while(maxCenterDislocation > MAXIMAL_ACCEPTABLE_MOD_CENTER_DISLOCATION);
			
			return modCenters;
		}
	}

	private double calculateMaxCenterDislocation(List<Matrix> modCenters,
			List<Matrix> newModCenters) {
		double maxCenterDislocation = 0;
		for (int i = 0; i < modCenters.size(); i++) {
			maxCenterDislocation = Math.max(maxCenterDislocation, Common.calculateEuclideanDistance(modCenters.get(i), newModCenters.get(i)));
		}
		
		return maxCenterDislocation;
	}

	private List<Matrix> correctModCenters(List<Matrix> modCenters,
			Map<Matrix, List<Sample>> samplesGroupedByMods) {
		List<Matrix> newModCenters = new ArrayList<>();
		
		for (Matrix modCenter : modCenters) {
			List<Sample> samplesOfMod = samplesGroupedByMods.get(modCenter);
			
			// there might occur a situation that some mods have no samples
			// so there's too many of them
			if (samplesOfMod.isEmpty()) {
				throw new EmptyModException();
			} else {
				Matrix newModCenter = Common.calculateMean(samplesOfMod);
				newModCenters.add(newModCenter);
			}
			
		}
		
		return newModCenters;
	}

	private List<Matrix> randomModCenters(List<? extends Sample> samples, int numberOfMods) {
		List<? extends Sample> centers = new ArrayList<>(samples);
		Collections.shuffle(samples);
		
		return centers
				.subList(0, numberOfMods)
				.stream()
				.map(Sample::getFeaturesMatrix)
				.collect(toList());
	}

	private Map<Matrix, List<Sample>> assignSamplesToMods(List<? extends Sample> samples,
			List<Matrix> modCenters) {
		Map<Matrix, List<Sample>> samplesGroupedByMods = modCenters
			.stream()
			.collect(toMap(identity(), a -> new ArrayList<>()));
		
		for (Sample sample : samples) {
			double minimalDistance = Double.MAX_VALUE;
			Matrix nearestMod = null;
			
			for (Matrix modCenter : modCenters) {
				double distance = Common.calculateEuclideanDistance(modCenter, sample.getFeaturesMatrix());
				if (distance < minimalDistance) {
					minimalDistance = distance;
					nearestMod = modCenter;
				}
			}
			
			samplesGroupedByMods
				.get(nearestMod)
				.add(sample);
		}
		
		return samplesGroupedByMods;
	}
	
	private Map<Matrix, Matrix> calculateCovarianceMatricesForMods(List<? extends Sample> samples, List<Matrix> modCenters) {
		Map<Matrix, List<Sample>> samplesGroupedByMods = assignSamplesToMods(samples, modCenters);
		Map<Matrix, Matrix> covarianceMatrices = new HashMap<>();
				
		for (Matrix modCenter : modCenters) {
			List<Sample> samplesOfMod = samplesGroupedByMods.get(modCenter);
			Matrix covarianceMatrix = Common.calculateCovarianceMatrix(samplesOfMod, modCenter);
			covarianceMatrices.put(modCenter, covarianceMatrix);
		}
		
		return covarianceMatrices;
	}
	
	private Double calculateError(List<Matrix> modCenters, List<SampleWithClass> samples) {
		Map<Matrix, List<Sample>> samplesGroupedByMods = assignSamplesToMods(samples, modCenters);
		double error = 0;
		
		for (Matrix modCenter : modCenters) {
			List<Sample> modSamples = samplesGroupedByMods.get(modCenter);
			
			double sum = modSamples
				.stream()
				.mapToDouble(x -> Common.calculateEuclideanDistance(modCenter, x.getFeaturesMatrix()))
				.sum();
			
			error += sum / modSamples.size();
		}
		
		return error / modCenters.size();
	}
	
	private boolean errorDecreasedSignificantly(List<Double> errors, int numberOfMods) {
		return Math.abs(errors.get(numberOfMods - 2) - errors.get(numberOfMods - 1)) > errors.get(numberOfMods - 2) * H;
	}
	
	private List<ClassStatisticData> transformToList(Map<Matrix, Matrix> covarianceMatricesForMods) {
		List<ClassStatisticData> classStatisticData = covarianceMatricesForMods
			.entrySet()
			.stream()
			.map(entry -> new ClassStatisticData(entry.getKey(), entry.getValue()))
			.collect(toList());
		return classStatisticData;
	}

	@Override
	public ModelClass classify(Sample sample) {
		if (!isTrained()) {
			throw new IllegalStateException("IClassifier has to be trained first");
		}
		
		ModelClass bestClass = null;
		double minimalDistance = Double.MAX_VALUE;
		
		for (ModelClass modelClass : classesStatisticData.keySet()) {
			double distance = calculateDistanceToClass(sample, classesStatisticData.get(modelClass));
			if (distance < minimalDistance) {
				minimalDistance = distance;
				bestClass = modelClass;
			}
		}
		
		return bestClass;
	}

	private double calculateDistanceToClass(Sample sample, ClassData classData) {
		double minimalDistance = Double.MAX_VALUE;
		
		for (ClassStatisticData classStatisticData : classData.classStatisticData) {
			double distance = IDistanceCalculator.calculate(sample, classStatisticData);
			if (distance < minimalDistance) {
				minimalDistance = distance;
			}
		}
		
		return minimalDistance;
	}
	
	@Override
	public boolean isTrained() {
		return classesStatisticData != null;
	}
	
	
	private static class ClassData {
		
		List<ClassStatisticData> classStatisticData;

		ClassData(List<ClassStatisticData> classStatisticData) {
			this.classStatisticData = classStatisticData;
		}
		
	}

}
