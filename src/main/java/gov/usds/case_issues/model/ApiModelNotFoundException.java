package gov.usds.case_issues.model;

public class ApiModelNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String _entityType;
	private final String _entityId;

	public ApiModelNotFoundException(String entityType, String entityId) {
		super();
		_entityType = entityType;
		_entityId = entityId;
	}

	public String getEntityType() {
		return _entityType;
	}

	public String getEntityId() {
		return _entityId;
	}

	@Override
	public String getMessage() {
		return String.format("%s '%s' was not found", _entityType, _entityId);
	}
}
