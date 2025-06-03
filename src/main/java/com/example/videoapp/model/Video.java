// INIZIO Video.java
package com.example.videoapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user; 

    public Video() {}

    public Video(String title, String description, boolean isPublic, User user) {
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.user = user;
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
// FINE Video.java