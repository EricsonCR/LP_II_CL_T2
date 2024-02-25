package org.cibertec.edu.pe.controller;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.cibertec.edu.pe.model.Detalle;
import org.cibertec.edu.pe.model.Producto;
import org.cibertec.edu.pe.model.Venta;
import org.cibertec.edu.pe.repository.IDetalleRepository;
import org.cibertec.edu.pe.repository.IProductoRepository;
import org.cibertec.edu.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({ "detalle", "total", "subtotal", "envio", "descuento", "mensaje" })
public class ProductoController {

	@Autowired
	private IProductoRepository productoRepository;
	@Autowired
	private IVentaRepository ventaRepository;
	@Autowired
	private IDetalleRepository detalleRepository;

	@GetMapping("/index")
	public String listado(Model model) {
		List<Producto> lista = new ArrayList<>();
		lista = productoRepository.findAll();
		model.addAttribute("productos", lista);
		return "index";
	}

	@GetMapping("/agregar/{idProducto}")
	public String agregar(Model model, @PathVariable(name = "idProducto", required = true) int idProducto) {

		boolean existe = false;
		double subtotal = 0, envio = 0, descuento = 0, total = 0;

		ArrayList<Detalle> ListaDetalle = (ArrayList<Detalle>) model.getAttribute("detalle");
		ArrayList<Producto> ListaProducto = (ArrayList<Producto>) productoRepository.findAll();
		Producto oProducto = new Producto();
		Detalle oDetalle = new Detalle();

		for (Producto p : ListaProducto) {
			if (p.getIdProducto() == idProducto) {
				oProducto = p;
				break;
			}
		}

		for (Detalle d : ListaDetalle) {
			if (d.getProducto().getIdProducto() == idProducto) {
				existe = true;
				d.setCantidad(d.getCantidad() + 1);
				d.setSubtotal(d.getCantidad() * d.getProducto().getPrecio());
			}
		}
		if (!existe) {
			oDetalle.setProducto(oProducto);
			oDetalle.setCantidad(1);
			oDetalle.setSubtotal(oProducto.getPrecio());
			ListaDetalle.add(oDetalle);
		}

		for (Detalle d : ListaDetalle) {
			subtotal += d.getSubtotal();
		}

		envio = subtotal * 5.0 / 100.0;
		descuento = subtotal * 2.0 / 100.0;
		total = subtotal + envio - descuento;

		model.addAttribute("subtotal", subtotal);
		model.addAttribute("envio", envio);
		model.addAttribute("descuento", descuento);
		model.addAttribute("total", total);
		model.addAttribute("detalle", ListaDetalle);
		return "redirect:/index";
	}

	@GetMapping("/eliminar/{idProducto}")
	public String eliminar(Model model, @PathVariable int idProducto) {

		ArrayList<Detalle> ListaDetalle = (ArrayList<Detalle>) model.getAttribute("detalle");
		ListaDetalle.remove(idProducto - 1);
		model.addAttribute("detalle", ListaDetalle);
		return "carrito";
	}

	@GetMapping("/carrito")
	public String carrito() {
		return "carrito";
	}

	@GetMapping("/pagar")
	public String pagar(Model model) {
		String mensaje = "";
		Venta oVenta = new Venta();
		// oVenta.setFechaRegistro(leerFechaHora());
		oVenta.setMontoTotal((double) model.getAttribute("total"));
		ventaRepository.save(oVenta);

		ArrayList<Detalle> ListaDetalle = (ArrayList<Detalle>) model.getAttribute("detalle");
		for (Detalle d : ListaDetalle) {
			d.setVenta(oVenta);
			detalleRepository.save(d);
		}

		mensaje = "ID VENTA: " + oVenta.getIdVenta() + " -- TOTAL: " + oVenta.getMontoTotal() + " -- COMPRA EXITOSA";
		model.addAttribute("mensaje", mensaje);

		ListaDetalle.clear();
		String total = "0.0";
		String subtotal = "0.0";
		String envio = "0.0";
		String descuento = "0.0";
		model.addAttribute("subtotal", subtotal);
		model.addAttribute("envio", envio);
		model.addAttribute("descuento", descuento);
		model.addAttribute("total", total);
		model.addAttribute("detalle", ListaDetalle);

		return "mensaje";
	}

	@PostMapping("/actualizarCarrito")
	public String actualizarCarrito(Model model) {
		double subtotal = 0, envio = 0, descuento = 0, total = 0;
		ArrayList<Detalle> ListaDetalle = (ArrayList<Detalle>) model.getAttribute("detalle");

		for (Detalle d : ListaDetalle) {
			subtotal += d.getSubtotal();
		}

		envio = subtotal * 5.0 / 100.0;
		descuento = subtotal * 2.0 / 100.0;
		total = subtotal + envio - descuento;

		model.addAttribute("subtotal", subtotal);
		model.addAttribute("envio", envio);
		model.addAttribute("descuento", descuento);
		model.addAttribute("total", total);
		model.addAttribute("detalle", ListaDetalle);
		return "carrito";
	}

	// Inicializacion de variable de la sesion
	@ModelAttribute("detalle")
	public List<Detalle> getCarrito() {
		return new ArrayList<Detalle>();
	}

	@ModelAttribute("total")
	public double getTotal() {
		return 0.0;
	}

	@ModelAttribute("subtotal")
	public double getSubTotal() {
		return 0.0;
	}

	@ModelAttribute("envio")
	public double getEnvio() {
		return 0.0;
	}

	@ModelAttribute("descuento")
	public double getDescuento() {
		return 0.0;
	}

	@ModelAttribute("mensaje")
	public String getMensaje() {
		return "";
	}

	Date leerFechaHora() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

		String dateInString = "7-Jun-2013";
		Date date = null;
		try {
			date = (Date) formatter.parse(dateInString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
}
