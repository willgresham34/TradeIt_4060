package edu.uga.cs.tradeit.models;

public class Category {

    private String id;
    private String name;
    private String createdBy;
    private int itemCount;

    public Category() { }

    public Category(String id, String name, String createdBy, int itemCount) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.itemCount = itemCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
