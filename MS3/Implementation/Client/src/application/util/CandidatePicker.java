package application.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Berechnet ElementRankings von Benutzerlisten und errechnet so die bestm�glichen Kandidaten f�r die Top-Liste
 * @author Leonid Vilents
 *
 */
public class CandidatePicker {
	
	/**
	 * Sortiert ein JSONArray von Benutzern aufsteigend nach deren Gesamtreputation.
	 * @param applicants JSONArray mit Benutzer-JSONObjects
	 * @return Ein JSONArray mit exakt 3 Benutzer-JSONObjects, die die besten Rankings besitzen
	 */
	public JSONArray getCandidatesForTopChoice(JSONArray applicants)
	{
		// Nach Gesamtreputation sortieren
		QuickSorter.sort(applicants);
		JSONArray arrayWithRankings = this.calculateRankings(this.getUpperQuartile(applicants));
		return arrayWithRankings;
	}
	
	//TODO: Kandidaten basierend auf Ranking ausw�hlen
	/*private JSONArray setTopCandidatesBasedOnRankings(JSONArray array)
	{
		
	}*/
	
	
	/**
	 * Berechnet anhand der Z�hlwerte im Objekt eines Arrays dessen Rankings
	 * im Hinblick auf Projekte, Projekt-Update-Kombination und Kommentare.
	 * @param array
	 * @return JSONArray mit erweiterten Benutzer-JSONObjects.
	 */
	private JSONArray calculateRankings (JSONArray array)
	{
		JSONArray arrayWithRankings = new JSONArray();
		
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject userObject = array.getJSONObject(i);
				JSONObject counters = userObject.getJSONObject("counters");
				
				JSONObject rankings = new JSONObject();
				// Ranking f�r Projekte
				int projectCount = counters.getInt("projects");
				int projectUpvotesCount = counters.getInt("projectUpvotes");
				int projectRanking = (int) Math.floor((projectCount + projectUpvotesCount) /2);
				rankings.put("projectRanking", projectRanking);
				
				// Ranking f�r Projekt-Update-Kombinationen
				int projectUpdatesCount = counters.getInt("projectUpdates");
				int projectUpdateUpvotesCount = counters.getInt("projectUpdateUpvotes");
				int partialRanking = (int) Math.floor((projectUpdatesCount + projectUpdateUpvotesCount) /2);
				int projectUpdatesRanking = (int) Math.floor((projectRanking + partialRanking) / 2);
				rankings.put("projectUpdatesRanking", projectUpdatesRanking);
				
				// Ranking f�r Kommentare
				int commentsCount = counters.getInt("comments");
				int commentUpvotesCount = counters.getInt("commentUpvotes");
				int commentsRanking = (int) Math.floor((commentsCount + commentUpvotesCount) / 2);
				rankings.put("commentsRanking", commentsRanking);
				
				userObject.put("rankings", rankings);
				
				arrayWithRankings.put(userObject);
			} catch (JSONException je) {
				je.printStackTrace();
				return null;
			}
		}
		return arrayWithRankings;
	}
	
	/**
	 * Gibt das obere Quartil des JSONArrays im Parameter als neues JSONArray zur�ck.
	 * @param array JSON-Array mit Benutzern
	 * @return JSONArray mit oberem Quartil des Parameter-Arrays
	 */
	private JSONArray getUpperQuartile(JSONArray array)
	{
		int n = array.length();
		int q3 = (int) (0.75 * (n+1));
		
		if (n == 3) {
			return array;
		}
		
		try {
			JSONArray q3array = new JSONArray();
			for (int i = q3 - 1; i < n; i++) {
				q3array.put(array.getJSONObject(i));
			}
			
			// Arrays kleiner 6 werden aufgef�llt
			if(q3array.length() < 6) {
				int j = q3-1;
				while(q3array.length() != 3 ){
					q3array.put(array.getJSONObject(--j));
				}
				return q3array;

			//Von gr��eren Arrays wird ein weiteres Oberes Quartil angefordert
			} else if (q3array.length() > 18) {	
				return getUpperQuartile(q3array);
			} else {
				return q3array;
			}
		} catch(JSONException je) {
			je.printStackTrace();
			return null;
		}
	}
}
