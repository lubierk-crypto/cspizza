package com.uniremington.cspizza;

import com.uniremington.cspizza.model.vo.CategoriaVO;
import com.uniremington.cspizza.model.vo.ClienteVO;
import com.uniremington.cspizza.model.vo.DetallePedidoVO;
import com.uniremington.cspizza.model.vo.FacturaVO;
import com.uniremington.cspizza.model.vo.PedidoVO;
import com.uniremington.cspizza.model.vo.ProductoVO;
import com.uniremington.cspizza.model.vo.UsuarioVO;
import com.uniremington.cspizza.service.CategoriaService;
import com.uniremington.cspizza.service.ClienteService;
import com.uniremington.cspizza.service.DetallePedidoService;
import com.uniremington.cspizza.service.FacturaService;
import com.uniremington.cspizza.service.InventarioService;
import com.uniremington.cspizza.service.PedidoService;
import com.uniremington.cspizza.service.ProductoService;
import com.uniremington.cspizza.service.UsuarioService;
import com.uniremington.cspizza.service.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CspizzaApplicationTests {

	@Autowired private CategoriaService categoriaService;
	@Autowired private ClienteService clienteService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ProductoService productoService;
	@Autowired private InventarioService inventarioService;
	@Autowired private PedidoService pedidoService;
	@Autowired private DetallePedidoService detallePedidoService;
	@Autowired private FacturaService facturaService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void productStockIsCanonicalAndInventoryCanBeDeletedAndRecreated() {
		CategoriaVO category = new CategoriaVO();
		category.setNombre("Test " + System.nanoTime());
		Long categoryId = categoriaService.guardar(category).getIdCategoria();

		ProductoVO product = new ProductoVO();
		product.setIdCategoria(categoryId);
		product.setNombre("Stock test " + System.nanoTime());
		product.setPrecioUnitario(new BigDecimal("12.50"));
		product.setStock(13);
		product = productoService.guardar(product);

		assertEquals(1, inventarioService.buscarPorProducto(product.getIdProducto()).size());
		assertEquals(13, inventarioService.buscarPorProducto(product.getIdProducto()).get(0).getCantidadActual());

		product.setStock(19);
		productoService.actualizar(product);
		assertEquals(19, inventarioService.buscarPorProducto(product.getIdProducto()).get(0).getCantidadActual());

		var inventory = inventarioService.buscarPorProducto(product.getIdProducto()).get(0);
		assertThrows(ConflictException.class, () -> inventarioService.guardar(inventory));
		inventarioService.eliminar(inventory.getIdInventario());
		assertTrue(inventarioService.buscarPorProducto(product.getIdProducto()).isEmpty());
		assertEquals(19, productoService.buscarPorId(product.getIdProducto()).orElseThrow().getStock());

		product.setStock(21);
		productoService.actualizar(product);
		assertTrue(inventarioService.buscarPorProducto(product.getIdProducto()).isEmpty());

		var recreated = new com.uniremington.cspizza.model.vo.InventarioVO();
		recreated.setIdProducto(product.getIdProducto());
		recreated.setCantidadActual(999);
		recreated.setCantidadMinima(0);
		recreated.setCantidadMaxima(50);
		recreated = inventarioService.guardar(recreated);
		assertEquals(21, recreated.getCantidadActual());
		assertEquals(21, productoService.buscarPorId(product.getIdProducto()).orElseThrow().getStock());
	}

	@Test
	@Transactional
	void orderTotalsUseProductPriceAndRecalculateWhenDetailsChange() {
		CategoriaVO category = new CategoriaVO();
		category.setNombre("Order test " + System.nanoTime());
		Long categoryId = categoriaService.guardar(category).getIdCategoria();

		ProductoVO product = new ProductoVO();
		product.setIdCategoria(categoryId);
		product.setNombre("Order product " + System.nanoTime());
		product.setPrecioUnitario(new BigDecimal("8.25"));
		product.setStock(10);
		product = productoService.guardar(product);

		ClienteVO client = new ClienteVO();
		client.setNombre("Order");
		client.setApellido("Customer");
		Long clientId = clienteService.guardar(client).getIdCliente();

		UsuarioVO user = new UsuarioVO();
		user.setNombreUsuario("test-" + System.nanoTime());
		user.setNombre("Test");
		user.setApellido("User");
		user.setRol("EMPLEADO");
		user.setCorreo(user.getNombreUsuario() + "@example.test");
		user.setPassword("test-password");
		user = usuarioService.guardar(user);
		assertTrue(user.getPassword().startsWith("$2"));
		Long userId = user.getIdUsuario();

		PedidoVO order = new PedidoVO();
		order.setIdCliente(clientId);
		order.setIdUsuario(userId);
		order.setTotalPedido(new BigDecimal("99999.99"));
		order = pedidoService.guardar(order);
		assertEquals(BigDecimal.ZERO, order.getTotalPedido());

		DetallePedidoVO detail = new DetallePedidoVO();
		detail.setIdPedido(order.getIdPedido());
		detail.setIdProducto(product.getIdProducto());
		detail.setCantidad(2);
		detail.setPrecioUnitario(BigDecimal.ONE);
		detail.setSubtotal(BigDecimal.ONE);
		detail = detallePedidoService.guardar(detail);
		assertEquals(new BigDecimal("8.25"), detail.getPrecioUnitario());
		assertEquals(new BigDecimal("16.50"), detail.getSubtotal());
		assertEquals(new BigDecimal("16.50"), pedidoService.buscarPorId(order.getIdPedido()).orElseThrow().getTotalPedido());

		detail.setCantidad(3);
		detail = detallePedidoService.actualizar(detail);
		assertEquals(new BigDecimal("24.75"), pedidoService.buscarPorId(order.getIdPedido()).orElseThrow().getTotalPedido());

		FacturaVO invoice = new FacturaVO();
		invoice.setIdPedido(order.getIdPedido());
		invoice.setNumeroFactura("TEST-" + System.nanoTime());
		invoice.setSubtotal(new BigDecimal("99999.99"));
		invoice.setIva(new BigDecimal("3.00"));
		invoice.setTotal(new BigDecimal("1.00"));
		invoice = facturaService.guardar(invoice);
		assertEquals(new BigDecimal("24.75"), invoice.getSubtotal());
		assertEquals(new BigDecimal("27.75"), invoice.getTotal());

		detallePedidoService.eliminar(detail.getIdDetallePedido());
		assertEquals(BigDecimal.ZERO, pedidoService.buscarPorId(order.getIdPedido()).orElseThrow().getTotalPedido());
	}
}