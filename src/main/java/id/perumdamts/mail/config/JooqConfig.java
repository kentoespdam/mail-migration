package id.perumdamts.mail.config;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

/**
 * Konfigurasi JOOQ DSLContext untuk complex read queries (CQRS query side).
 *
 * <p>JOOQ berpartisipasi dalam Spring transaction via {@link TransactionAwareDataSourceProxy}.
 * Semua query JOOQ akan berjalan dalam transaksi Spring yang aktif (jika ada),
 * atau membuka koneksi baru dari pool jika tidak dalam transaksi.
 */
@Configuration
public class JooqConfig {

    @Bean
    public DataSourceConnectionProvider connectionProvider(DataSource dataSource) {
        return new DataSourceConnectionProvider(
                new TransactionAwareDataSourceProxy(dataSource)
        );
    }

    @Bean
    public DefaultDSLContext dslContext(DataSourceConnectionProvider connectionProvider) {
        return new DefaultDSLContext(jooqConfiguration(connectionProvider));
    }

    private DefaultConfiguration jooqConfiguration(DataSourceConnectionProvider connectionProvider) {
        DefaultConfiguration config = new DefaultConfiguration();
        config.setConnectionProvider(connectionProvider);
        config.setSQLDialect(SQLDialect.MARIADB);
        return config;
    }
}
