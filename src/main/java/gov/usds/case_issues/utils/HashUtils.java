package gov.usds.case_issues.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to make it easier to use Hashs
 */
public class HashUtils {
	private static final Logger LOG = LoggerFactory.getLogger(HashUtils.class);

	private HashUtils() {
		throw new IllegalStateException("Utility class");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Optional<?> descend(Object o, List<String> path) {
		Object curr = o;
		for (String pathElement : path) {
			LOG.debug("Looking for {} in {}", pathElement, curr);
			if (curr instanceof Map) {
				Map<String, Object> cast = Map.class.cast(curr);
				curr = cast.get(pathElement);
			}
			else if (curr instanceof Collection) {
				return ((Collection<String>) curr).stream().filter(pathElement::equals).findFirst();
			}
			else if (curr instanceof String) {
				return pathElement.equals(curr) ? Optional.of((String) curr) : Optional.empty();
			}
		}
		LOG.debug("Finished path traversal with {}", curr);
		if (curr instanceof Collection && ((Collection) curr).size() == 1) {
			Iterator<String> it = ((Iterable) curr).iterator();
			return Optional.ofNullable(it.next());
		} else {
			return Optional.ofNullable(curr);
		}
	}
}
