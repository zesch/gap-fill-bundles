package de.unidue.ltl.gapfill.util;

public class Substitute implements Comparable<Substitute> {
	
	private String name;
	private double value;

	public Substitute(String name, double value){
		this.name = name;
		this.value = value;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setValue(double value){
		this.value = value;
	}
	
	public double getValue(){
		return value;
	}

	@Override
	public int compareTo(Substitute other) {
		if(other.getValue() > value)
			return -1;
		if(other.getValue() < value)
			return 1;
		return 0;
	}
}
