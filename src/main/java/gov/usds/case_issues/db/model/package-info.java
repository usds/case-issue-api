/**
 * Hibernate/JPA models.
 */
@TypeDefs({
	@TypeDef(defaultForType=Map.class, typeClass=JsonBinaryType.class, name="jsonb"),
})
package gov.usds.case_issues.db.model;

import java.util.Map;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
