createuser -U "$POSTGRES_USER" -w case_issue_migrations
createuser -U "$POSTGRES_USER" -w case_issue_api
createdb -U "$POSTGRES_USER" -w case_issues --maintenance-db="$POSTGRES_DB"

psql -v ON_ERROR_STOP=1  -U "$POSTGRES_USER" case_issues <<-SQL
    CREATE SCHEMA IF NOT EXISTS case_issue_main;
    GRANT ALL PRIVILEGES ON SCHEMA case_issue_main TO case_issue_migrations;
    GRANT USAGE ON SCHEMA case_issue_main TO case_issue_api;
    ALTER DEFAULT PRIVILEGES FOR USER case_issue_migrations IN SCHEMA case_issue_main
        GRANT SELECT, INSERT, DELETE, UPDATE, TRUNCATE ON TABLES TO case_issue_api;
    ALTER DEFAULT PRIVILEGES FOR USER case_issue_migrations IN SCHEMA case_issue_main
        GRANT SELECT, UPDATE ON SEQUENCES TO case_issue_api;
    GRANT USAGE ON LANGUAGE plpgsql to case_issue_api;
SQL
