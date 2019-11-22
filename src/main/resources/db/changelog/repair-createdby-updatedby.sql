UPDATE case_attachment
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_management_system
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_management_system
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE case_type
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_type
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE trouble_case
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE trouble_case
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE case_issue
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_issue
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE case_snooze
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_snooze
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE attachment_subtype
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE attachment_subtype
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE case_attachment_association
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_attachment_association
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

UPDATE case_issue_upload
SET created_by=substring(created_by from 1 for position(';' in created_by) - 1)
where created_by like '%;%';

UPDATE case_issue_upload
SET updated_by=substring(updated_by from 1 for position(';' in updated_by) - 1)
where updated_by like '%;%';

