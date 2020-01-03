package gov.usds.case_issues.model;

import gov.usds.case_issues.db.model.UserInformation;

/**
 * API definition for UserInformation
 */
public class SerializedUserInformation {

    private String id;
	private String name;

	public SerializedUserInformation(UserInformation wrapped) {
		this(wrapped.getId(), wrapped.getPrintName());
	}

	public SerializedUserInformation(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return id;
	}
}
