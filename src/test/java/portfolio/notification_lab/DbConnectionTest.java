package portfolio.notification_lab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DbConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void postgresConnectionTest() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("connection = " + connection);
            System.out.println("catalog = " + connection.getCatalog());

            assertThat(connection).isNotNull();
        }
    }

}
