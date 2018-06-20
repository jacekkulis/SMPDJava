package pl.jacekkulis.classifier;

import Jama.Matrix;
import pl.jacekkulis.model.Class;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleClass;
import pl.jacekkulis.utils.ClassData;
import pl.jacekkulis.utils.MathUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NearestMean implements IClassifier {

	private Map<Class, ClassData> classesStatisticData;
	
	@Override
	public void train(List<SampleClass> trainSamples) {
		classesStatisticData = calculateStatisticDataForClasses(trainSamples);
	}

	private Map<Class, ClassData> calculateStatisticDataForClasses(List<SampleClass> trainSamples) {
		Map<Class, List<SampleClass>> samplesGroupedByClass = groupSamplesByClass(trainSamples);
		Map<Class, ClassData> classesStatisticData = new HashMap<>();
		
		for (Class aClass : samplesGroupedByClass.keySet()) {
			List<SampleClass> samplesOfClass = samplesGroupedByClass.get(aClass);
			ClassData classData = calculateClassStatisticDataFrom(samplesOfClass);
			classesStatisticData.put(aClass, classData);
		}
		
		return classesStatisticData;
	}

	private Map<Class, List<SampleClass>> groupSamplesByClass(
			List<SampleClass> trainSamples) {
		return trainSamples
				.stream()
				.collect(Collectors.groupingBy(SampleClass::getaClass));
	}
	
	private ClassData calculateClassStatisticDataFrom(List<SampleClass> samplesOfClass) {
		Matrix mean = MathUtil.calculateMean(samplesOfClass);
		Matrix covarianceMatrix = MathUtil.calculateCovarianceMatrix(samplesOfClass, mean);
		
		return new ClassData(mean, covarianceMatrix);
	}
	
	@Override
	public Class classify(Sample sample) {
		if (!isTrained()) {
			throw new IllegalStateException("IClassifier has to be trained first");
		}
		
		double minimalMachalonobisDistance = Double.MAX_VALUE;
		Class nearestMeanClass = null;
		
		for (Entry<Class, ClassData> entry : classesStatisticData.entrySet()) {
			double machalonobisDistance = MathUtil.calculateMahalonobisDistance(sample, entry.getValue());
			if (machalonobisDistance < minimalMachalonobisDistance) {
				minimalMachalonobisDistance = machalonobisDistance;
				nearestMeanClass = entry.getKey();
			}
		}
		
		return nearestMeanClass;
	}

	@Override
	public boolean isTrained() {
		return classesStatisticData != null;
	}
	
}
