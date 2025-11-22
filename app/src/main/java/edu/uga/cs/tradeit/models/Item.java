package edu.uga.cs.tradeit.models;

public class Item {

    private String id;
    private String name;
    private Double price;      // null or 0 if free
    private String categoryId;
    private String sellerId;   // uid
    private long createdAt;
    private String status;

    public Item() { }

    public Item(String id, String name, Double price, boolean free,
                String categoryId, String sellerId, long createdAt, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
