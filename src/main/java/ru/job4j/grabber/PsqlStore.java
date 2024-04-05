package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private final Connection connection;

    public PsqlStore(Properties config) {
        try {
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

    public PsqlStore(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Post post) {
        try (var statement = connection.prepareStatement(
                """
                        INSERT INTO post(name, text, link, created)
                        VALUES (?, ?, ?, ?)
                        ON CONFLICT (link)
                        DO UPDATE SET name = EXCLUDED.name, text = EXCLUDED.text, created = EXCLUDED.created;
                        """,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (var result = statement.getGeneratedKeys()) {
                if (result.next()) {
                    post.setId(result.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        var list = new ArrayList<Post>();
        try (var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT * FROM post;")) {
                while (result.next()) {
                    var post = new Post(
                            result.getString("name"),
                            result.getString("link"),
                            result.getTimestamp("created").toLocalDateTime(),
                            result.getString("text"));
                    post.setId(result.getInt("id"));
                    list.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post rsl = null;
        try (var statement = connection.prepareStatement("SELECT * FROM post WHERE id = ?;")) {
            statement.setInt(1, id);
            if (statement.execute()) {
                try (var resultSet = statement.getResultSet()) {
                    if (resultSet.next()) {
                        rsl = new Post(
                                resultSet.getString("name"),
                                resultSet.getString("link"),
                                resultSet.getTimestamp("created").toLocalDateTime(),
                                resultSet.getString("text"));
                        rsl.setId(resultSet.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}