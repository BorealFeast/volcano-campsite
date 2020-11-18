package com.borealfeast.reservation.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest()
@ContextConfiguration(classes = {IntegrationTestConfiguration.class})
public abstract class BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void before() throws SQLException {
        java.sql.Connection c = dataSource.getConnection();
        Statement s = c.createStatement();
        s.execute("TRUNCATE TABLE availabilities CASCADE;");
        s.execute("TRUNCATE TABLE availabilities_bookings CASCADE;");
        s.execute("TRUNCATE TABLE booking_by_short_period_entity CASCADE;");
        s.execute("TRUNCATE TABLE bookings CASCADE;");
        s.execute("TRUNCATE TABLE reservation_entity CASCADE;");
        s.close();
        c.close();
    }
}
