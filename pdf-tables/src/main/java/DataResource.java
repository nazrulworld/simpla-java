import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class DataResource {
	@JsonProperty("verber")
	private ArrayList<ArrayList<String>> verberList;

	@JsonProperty("substantiver")
	private ArrayList<ArrayList<String>> substantiverList;

	@JsonProperty("adjektiver")
	private ArrayList<ArrayList<String>> adjektiverList;

	public ArrayList<ArrayList<String>> getVerberList() {
		return verberList;
	}

	public void setVerberList(ArrayList<ArrayList<String>> verberList) {
		this.verberList = verberList;
	}

	public ArrayList<ArrayList<String>> getSubstantiverList() {
		return substantiverList;
	}

	public void setSubstantiverList(ArrayList<ArrayList<String>> substantiverList) {
		this.substantiverList = substantiverList;
	}

	public ArrayList<ArrayList<String>> getAdjektiverList() {
		return adjektiverList;
	}

	public void setAdjektiverList(ArrayList<ArrayList<String>> adjektiverList) {
		this.adjektiverList = adjektiverList;
	}
}


