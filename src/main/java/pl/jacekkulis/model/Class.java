package pl.jacekkulis.model;

import java.util.ArrayList;
import java.util.List;

public class Class {

	private final String name;
	private final List<Sample> samples = new ArrayList<>();

	public Class(String name) {
		this.name = name;
	}

	public void addSample(Sample sample) {
		this.samples.add(sample);
	}
	
	public List<Sample> getSamples() {
		return samples;
	}
	
	public boolean contains(Sample sample) {
		return samples.contains(sample);
	}

	public String getName() {
		return name;
	}

}
