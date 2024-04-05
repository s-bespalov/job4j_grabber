package ru.job4j.grabber;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

@Disabled
class PsqlStoreTest {

    private static Connection connection;

    private static String selectPostFromDataBaseAsString(String link) throws SQLException {
        var rsl = "";
        try (var statement = connection.prepareStatement("SELECT * FROM post WHERE link = ?")) {
            statement.setString(1, link);
            statement.execute();
            try (var resultSet = statement.getResultSet()) {
                if (resultSet.next()) {
                    var post = new Post(
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getTimestamp("created").toLocalDateTime(),
                            resultSet.getString("text"));
                    post.setId(resultSet.getInt("id"));
                    rsl = post.toString();
                }
            }
        }
        return rsl;
    }

    @BeforeAll
    public static void initConnection() {
        try (var in = PsqlStore.class.getClassLoader()
                .getResourceAsStream("liquibase_test.properties")) {
            var config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void tearDownAfterTests() throws Exception {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (var statement = connection.prepareStatement("DELETE FROM post")) {
            statement.execute();
        }
    }

    @Test
    public void whenSaveNewPost() throws SQLException {
        var post = new Post("title1", "http://site.com/link1",
                LocalDateTime.of(2024, 3, 8, 13, 4, 0),
                "description1");
        var store = new PsqlStore(connection);
        store.save(post);
        var expected = post.toString();
        var result = selectPostFromDataBaseAsString(post.getLink());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenSaveEqualLinks() throws SQLException {
        var post = new Post("title1", "http://site.com/link2",
                LocalDateTime.of(2024, 3, 8, 13, 4, 0),
                "description1");
        var post2 = new Post("title2", "http://site.com/link2",
                LocalDateTime.of(2024, 1, 8, 13, 4, 0),
                "description2");
        var store = new PsqlStore(connection);
        store.save(post);
        store.save(post2);
        var excepted = post2.toString();
        var result = selectPostFromDataBaseAsString(post2.getLink());
        assertThat(post.getId()).isNotEqualTo(0);
        assertThat(post.getId()).isEqualTo(post2.getId());
        assertThat(result).isEqualTo(excepted);
    }

    @Test
    public void whenGetAll() {
        var post = new Post("title1", "http://site.com/link3",
                LocalDateTime.of(2024, 3, 8, 13, 4, 0),
                "description1");
        var post2 = new Post("title2", "http://site.com/link4",
                LocalDateTime.of(2024, 1, 8, 13, 4, 0),
                "description2");
        var store = new PsqlStore(connection);
        store.save(post);
        store.save(post2);
        var result = store.getAll();
        assertThat(result).containsOnly(post, post2);
    }

    @Test
    public void whenFindById() {
        var post = new Post("title5", "http://site.com/link5",
                LocalDateTime.of(2024, 3, 8, 12, 4, 0),
                "description5");
        var store = new PsqlStore(connection);
        store.save(post);
        var rsl = store.findById(post.getId());
        assertThat(rsl.toString()).isEqualTo(post.toString());
    }
}