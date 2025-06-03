// INIZIO NuovoVideoInputDto.java
package com.example.videoapp.dto;

public class NuovoVideoInputDto {
    private Long userId;  
    private String title;
    private String description;
    private boolean isPublic;

    public Long getUserId() { return userId; }  
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}
// FINE NuovoVideoInputDto.java