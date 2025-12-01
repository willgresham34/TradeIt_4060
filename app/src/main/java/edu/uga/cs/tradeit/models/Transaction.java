package edu.uga.cs.tradeit.models;

public class Transaction {

    private String id;
    private String itemId;
    private String categoryId;
    private String sellerId;
    private String buyerId;
    private Double price;
    private boolean free;
    private long createdAt;
    private String status; // PENDING or COMPLETED
    private Long completedAt;

    private String sellerName;
    private String buyerName;

    public Transaction() { }

    public Transaction(String id, String itemId, String categoryId,
                       String sellerId, String buyerId,
                       Double price, boolean free,
                       long createdAt, String status, Long completedAt) {
        this.id = id;
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.price = price;
        this.free = free;
        this.createdAt = createdAt;
        this.status = status;
        this.completedAt = completedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public boolean isFree() { return free; }
    public void setFree(boolean free) { this.free = free; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
}
