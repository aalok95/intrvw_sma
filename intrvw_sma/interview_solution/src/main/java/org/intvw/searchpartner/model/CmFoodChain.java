/* *************************************************************
 *
 * COPYRIGHT (c) 2018-19 Saurabh Agrawal
 * All Rights Reserved. Saurabh Agrawal - Confidential.
 *
 * Date : Aug 04, 2018
 *
 * *************************************************************/
package org.intvw.searchpartner.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * 
 * @author saurabh_agrawal
 *
 */
@JacksonXmlRootElement(localName = "cmfoodchain")
public class CmFoodChain implements Convertable {
	@JacksonXmlElementWrapper(useWrapping = false)
	Branch branch;
	
	@JacksonXmlElementWrapper(useWrapping = false)
	Orders orders;
	
	List<Branch> branches;

	public CmFoodChain() {
	}

	public CmFoodChain(Branch branch, Orders orders) {
		this.branch = branch;
		this.orders = orders;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Orders getOrders() {
		return orders;
	}

	public void setOrders(Orders orders) {
		this.orders = orders;
	}

	public List<Branch> getBranches() {
		return branches;
	}

	public void setBranches(List<Branch> branches) {
		this.branches = branches;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CmFoodChain [branch=");
		builder.append(branch);
		builder.append(", orders=");
		builder.append(orders);
		builder.append(", branches=");
		builder.append(branches);
		builder.append("]");
		return builder.toString();
	}
}
