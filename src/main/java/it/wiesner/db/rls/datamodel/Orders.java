package it.wiesner.db.rls.datamodel;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long orderId;
	
	private String orderNumber;
	
	private Long tenantId;
	
	private LocalDate orderDate;
	
	private String orderStatus;
	
	@Column(name = "customername", length = 100)
	private String customername;

	@Override
	public String toString() {
		return "Orders [orderId=" + orderId + ", orderNumber=" + orderNumber + ", tenantId=" + tenantId 
				+ ", orderDate=" + orderDate + ", orderStatus=" + orderStatus + ", customername=" + customername + "]";
	}

	// Getters
	public Long getOrderId() {
		return orderId;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public String getCustomername() {
		return customername;
	}

	// Setters
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setCustomername(String customername) {
		this.customername = customername;
	}

}
