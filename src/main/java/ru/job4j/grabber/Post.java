package ru.job4j.grabber;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public class Post {
    private int id;

    private String title;

    private String link;

    private String description;

    private LocalDateTime created;

    public Post(String title, String link, LocalDateTime created, String description) {
        this.title = title;
        this.link = link;
        this.created = created;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Post post = (Post) o;

        if (id != 0) {
            return id == post.id;
        }

        if (id != post.id) {
            return false;
        }
        if (!Objects.equals(title, post.title)) {
            return false;
        }
        if (!Objects.equals(link, post.link)) {
            return false;
        }
        return Objects.equals(created, post.created);
    }

    @Override
    public int hashCode() {
        int result = id;
        if (id == 0) {
            result = (title != null ? title.hashCode() : 0);
            result = 31 * result + (link != null ? link.hashCode() : 0);
            result = 31 * result + (created != null ? created.hashCode() : 0);
        }
        return result;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Post.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("title='" + title + "'")
                .add("link='" + link + "'")
                .add("description='" + description + "'")
                .add("created=" + created)
                .toString();
    }
}
