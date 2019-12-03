package gov.usds.case_issues.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Wrap any {@link DataSource} bean in retry configuration.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RetryableDataSourceBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof DataSource) {
			bean = new RetryableDataSource((DataSource)bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	private static class RetryableDataSource extends AbstractDataSource {

		private DataSource delegate;
		private final int maxAttempts = 10;
		private final double multiplier = 2.3;
		private final int maxDelay = 30000;

		public RetryableDataSource(DataSource delegate) {
			this.delegate = delegate;
		}

		@Override
		@Retryable(maxAttempts=maxAttempts, backoff=@Backoff(multiplier=multiplier, maxDelay=maxDelay))
		public Connection getConnection() throws SQLException {
			return delegate.getConnection();
		}

		@Override
		@Retryable(maxAttempts=maxAttempts, backoff=@Backoff(multiplier=multiplier, maxDelay=maxDelay))
		public Connection getConnection(String username, String password)
				throws SQLException {
			return delegate.getConnection(username, password);
		}

	}
}

