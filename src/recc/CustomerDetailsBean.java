/*
 *  * To change this license header, choose License Headers in Project Properties.
 *   * To change this template file, choose Tools | Templates
 *    * and open the template in the editor.
 *     */
package recc;

/**
 *  *
 *   * @author ch-e00793
 *    */
public class CustomerDetailsBean {

    private String customerId;
    private String functionalName;
    private String packName;
    private int totalQuota;
    private long balance;
    private long expiryDate;

    public CustomerDetailsBean() {
    }

    public CustomerDetailsBean(String customerId, String functionalName, String packName, int totalQuota, long balance, long expiryDate) {
        this.customerId = customerId;
        this.functionalName = functionalName;
        this.totalQuota = totalQuota;
        this.balance = balance;
        this.packName = packName;
        this.expiryDate = expiryDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFunctionalName() {
        return functionalName;
    }

    public void setFunctionalName(String functionalName) {
        this.functionalName = functionalName;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public int getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(int totalQuota) {
        this.totalQuota = totalQuota;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }
}
