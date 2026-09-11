package com.retailable.supermarket.inventory;

public class InventoryRow {
    private Long productId;
    private Integer currentQty;
    private Integer reservedQty;

    public Long getProductId(){return productId;} public void setProductId(Long productId){this.productId=productId;}
    public Integer getCurrentQty(){return currentQty;} public void setCurrentQty(Integer currentQty){this.currentQty=currentQty;}
    public Integer getReservedQty(){return reservedQty;} public void setReservedQty(Integer reservedQty){this.reservedQty=reservedQty;}
    public int availableQty(){return currentQty-reservedQty;}
}

