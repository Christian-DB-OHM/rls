package it.wiesner.db.rls.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.wiesner.db.rls.datamodel.Orders;
import it.wiesner.db.rls.datamodel.OrdersRepository;
import it.wiesner.db.rls.session.RlsSessionHolder;

@RestController
@Component
public class OrdersController {

	Logger log = LoggerFactory.getLogger(OrdersController.class);

	@Autowired
	private OrdersRepository ordersRepository;

	public List<Orders> getOrders() throws IOException {
		List<Orders> orders = new ArrayList<Orders>();
		Iterator<Orders> iter = ordersRepository.findAll().iterator();
		while (iter.hasNext()) {
			Orders order = iter.next();
			orders.add(order);
		}
		return orders;
	}

	@GetMapping("/rls/orders")
	@Transactional(readOnly = true)
	public ResponseEntity<List<Orders>> getAllOrders() throws IOException {
		// Session wurde bereits beim Login gesetzt
		RlsSessionHolder.RlsSession session = RlsSessionHolder.getRlsSession();
		if (session == null) {
			log.warn("No RLS session found - user not logged in");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		log.info("Fetching orders for tenant: {}", session.tenantId);
		List<Orders> orders = getOrders();
		log.info("Retrieved {} orders", orders.size());
		return ResponseEntity.ok(orders);
	}

	@PostMapping("/rls/orders")
	@Transactional
	public ResponseEntity<Orders> createOrder(@RequestBody Orders order) {
		// Session wurde bereits beim Login gesetzt
		RlsSessionHolder.RlsSession session = RlsSessionHolder.getRlsSession();
		if (session == null) {
			log.warn("No RLS session found - user not logged in");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		// Verify user is creating order for their own tenant
		if (!order.getTenantId().equals(session.tenantId)) {
			log.warn("Tenant mismatch: trying to create order for tenant {}, but session has {}", 
				order.getTenantId(), session.tenantId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		try {
			log.info("Creating order for tenant: {}", order.getTenantId());
			Orders savedOrder = ordersRepository.save(order);
			log.info("Successfully created order with ID: {} for tenant: {}", savedOrder.getOrderId(), savedOrder.getTenantId());
			return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
		} catch (Exception e) {
			log.error("Error creating order for tenant: {}", order.getTenantId(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/rls/orders/{id}")
	@Transactional
	public ResponseEntity<Orders> updateOrder(@PathVariable Long id, @RequestBody Orders order) {
		// Session wurde bereits beim Login gesetzt
		RlsSessionHolder.RlsSession session = RlsSessionHolder.getRlsSession();
		if (session == null) {
			log.warn("No RLS session found - user not logged in");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		// Verify user is updating order for their own tenant
		if (!order.getTenantId().equals(session.tenantId)) {
			log.warn("Tenant mismatch: trying to update order for tenant {}, but session has {}", 
				order.getTenantId(), session.tenantId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		try {
			log.info("Updating order {} for tenant: {}", id, order.getTenantId());
			@SuppressWarnings("null")
			Optional<Orders> existingOrder = ordersRepository.findById(id);
			if (existingOrder.isPresent()) {
				Orders updatedOrder = ordersRepository.save(order);
				log.info("Successfully updated order with ID: {} for tenant: {}", id, order.getTenantId());
				return ResponseEntity.ok(updatedOrder);
			} else {
				log.warn("Order with ID: {} not found for tenant: {}", id, order.getTenantId());
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			log.error("Error updating order {} for tenant: {}", id, order.getTenantId(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@SuppressWarnings("null")
	@DeleteMapping("/rls/orders/{id}")
	@Transactional
	public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
		// Session wurde bereits beim Login gesetzt
		RlsSessionHolder.RlsSession session = RlsSessionHolder.getRlsSession();
		if (session == null) {
			log.warn("No RLS session found - user not logged in");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			log.info("Deleting order: {}", id);
			Optional<Orders> existingOrder = ordersRepository.findById(id);
			if (existingOrder.isPresent()) {
				// RLS will automatically restrict deletion to orders visible to this session
				ordersRepository.deleteById(id);
				log.info("Successfully deleted order with ID: {}", id);
				return ResponseEntity.ok().build();
			} else {
				log.warn("Order with ID: {} not found for deletion", id);
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			log.error("Error deleting order: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
