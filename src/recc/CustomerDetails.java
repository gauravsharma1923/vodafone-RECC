/*
 *  * To change this license header, choose License Headers in Project Properties.
 *   * To change this template file, choose Tools | Templates
 *    * and open the template in the editor.
 *     */
package recc;

import java.util.ArrayList;

/**
 *  *
 *   * @author ch-e00793
 *    */
public class CustomerDetails {

    private String errorMsg;
    private ArrayList<CustomerDetailsBean> listCustomerDetails;

    public CustomerDetails() {
    }

    public CustomerDetails(String errorMsg, ArrayList<CustomerDetailsBean> listCustomerDetails) {
        this.errorMsg = errorMsg;
        this.listCustomerDetails = listCustomerDetails;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public ArrayList<CustomerDetailsBean> getListCustomerDetails() {
        return listCustomerDetails;
    }

    public void setListCustomerDetails(ArrayList<CustomerDetailsBean> listCustomerDetails) {
        this.listCustomerDetails = listCustomerDetails;
    }
}
