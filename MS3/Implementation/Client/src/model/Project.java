package model;

import java.util.Date;

/**
 * Modell eines Projektes
 * @author L�on
 */
public class Project {
	
	private int pId;
	private int uId;
	
	public String title;
	
	public Date createdOn;
	
	public String content;
	
	public String status;
	
	public int repValue;
	
	public Project(int pId, int uId) {
		this.pId = pId;
		this.uId = uId;
	}
	
	public int getPId() {
		return this.pId;
	}
	
	public int getUId() {
		return this.uId;
	}
}
