WITH all_action_signatures AS (
    SELECT updated_by as user_id, updated_at as date_time from case_management_system
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from case_type
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from trouble_case
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from case_issue
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from case_snooze
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from attachment_subtype
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from case_attachment_association
UNION ALL
    SELECT updated_by as user_id, updated_at as date_time from case_issue_upload
UNION ALL
    SELECT created_by, created_at from case_management_system
UNION ALL
    SELECT created_by, created_at from case_type
UNION ALL
    SELECT created_by, created_at from trouble_case
UNION ALL
    SELECT created_by, created_at from case_issue
UNION ALL
    SELECT created_by, created_at from case_snooze
UNION ALL
    SELECT created_by, created_at from attachment_subtype
UNION ALL
    SELECT created_by, created_at from case_attachment_association
UNION ALL
    SELECT created_by, created_at from case_issue_upload
UNION ALL
    SELECT created_by, created_at from case_attachment
),
signature_summary AS (
    select user_id, min(date_time) as earliest_seen, max(date_time) last_seen
    from all_action_signatures
    where user_id like '%;%'
    group by user_id
),
user_entries as (
    select
	    substring(user_id from 1 for position(';' in user_id) - 1) as user_id,
	    substring(user_id from position(';' in user_id) + 1) as print_name,
	    earliest_seen,
        last_seen
    from signature_summary
)
insert into user_information (internal_id, created_at, updated_at, user_id, print_name, last_seen)
select
	row_number() over (order by user_id) as internal_id,
	current_timestamp as created_at,
	current_timestamp as updated_at,
	user_id,
	print_name,
	last_seen
from user_entries
where not exists (select * from user_information ui where ui.user_id = user_entries.user_id)
returning *
