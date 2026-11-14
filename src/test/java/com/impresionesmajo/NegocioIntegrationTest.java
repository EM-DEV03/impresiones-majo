package com.impresionesmajo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import com.impresionesmajo.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username="admin",roles="ADMINISTRADOR")
class NegocioIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired VentaService ventas;
    @Autowired CompraService compras;
    @Autowired InventarioService inventario;
    @Autowired ProductoService productos;
    @Autowired List<CatalogoService> catalogos;
    @Autowired UsuarioService usuarios;
    @Autowired GastoService gastos;
    @Autowired CapitalService capital;
    @Autowired InversionService inversiones;
    @Autowired ReporteService reportes;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @Autowired WebApplicationContext context;
    long producto,otro,servicio,proveedor;
    static String hash;
    @BeforeEach void preparar() {
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        // Exclusivamente H2 en memoria: nunca usa la base de negocio.
        assertThat(jdbc.getDataSource()).isNotNull();
        assertThat(jdbc.queryForObject("SELECT DATABASE()",String.class)).isEqualToIgnoringCase("impresiones_majo_test");
        for(String table:List.of("movimiento_inventario","detalle_venta","detalle_compra","venta","compra","gasto","aporte_capital","inversion_equipo","usuario_rol","usuario")) jdbc.update("DELETE FROM "+table);
        jdbc.update("DELETE FROM producto WHERE codigo LIKE 'TEST-MAJO-%'");
        jdbc.update("DELETE FROM proveedor WHERE nombre='Temporal Majo'");
        jdbc.update("UPDATE proveedor SET activo=1");
        jdbc.update("UPDATE categoria SET activo=1");
        jdbc.update("UPDATE producto SET stock_actual=0,costo_promedio=100,precio_venta=200,activo=1");
        jdbc.update("UPDATE servicio SET activo=1");
        if(hash==null) hash=encoder.encode("UnaClaveSegura123!");
        for(String user:List.of("admin","vendedor")) {
            jdbc.update("INSERT INTO usuario(nombre_usuario,nombre_completo,password_hash) VALUES (?,?,?)",user,user,hash);
            jdbc.update("INSERT INTO usuario_rol(usuario_id,rol_id) SELECT u.id,r.id FROM usuario u CROSS JOIN rol r WHERE u.nombre_usuario=? AND r.codigo=?",user,user.equals("admin")?"ADMINISTRADOR":"VENDEDOR");
        }
        producto=id("producto","codigo","PAP-001");otro=id("producto","codigo","PAP-002");
        servicio=id("servicio","codigo","SER-001");proveedor=id("proveedor","nombre","Proveedor de Papel Demo");
        // Cada prueba puede editar el catálogo. Restaurar esta referencia evita
        // que una prueba dependa del orden en que JUnit ejecute los métodos.
        jdbc.update("UPDATE producto SET proveedor_preferido_id=? WHERE codigo IN ('PAP-001','PAP-002')",proveedor);
    }
    long id(String table,String column,String value){return jdbc.queryForObject("SELECT id FROM "+table+" WHERE "+column+"=?",Long.class,value);}
    BigDecimal stock(long p){return jdbc.queryForObject("SELECT stock_actual FROM producto WHERE id=?",BigDecimal.class,p);}
    OperacionForm venta(long p,String cantidad){
        var f=new OperacionForm();var l=new OperacionForm.Linea();l.setProductoId(p);l.setCantidad(new BigDecimal(cantidad));f.setLineas(List.of(l));f.setValorRecibido(new BigDecimal("100000"));return f;
    }
    void entrada(String cantidad){inventario.ajustar(producto,new BigDecimal(cantidad),"AJUSTE_ENTRADA","Conteo inicial",new BigDecimal("100"));}
    Map<String,String> productoInicial(String codigo,String unidades) {
        var data=new HashMap<String,String>();data.put("categoria_id",String.valueOf(jdbc.queryForObject("SELECT categoria_id FROM producto WHERE id=?",Long.class,producto)));
        data.put("codigo",codigo);data.put("nombre","Cartulina de prueba");data.put("tipo","MERCANCIA");data.put("unidad_medida","UNIDAD");
        data.put("precio_venta","1.500");data.put("stock_minimo","9");data.put("activo","true");
        data.put("unidades_iniciales",unidades);data.put("costo_inicial","1.000");return data;
    }
    @Test void unidadesInicialesComprasVentasYAnulacionesConservanElSaldo() throws Exception {
        var data=productoInicial("TEST-MAJO-INIT","9");productos.guardar(null,data);long p=id("producto","codigo","TEST-MAJO-INIT");
        assertThat(stock(p)).isEqualByComparingTo("9");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario WHERE producto_id=?",Integer.class,p)).isEqualTo(1);
        long v=ventas.crear(venta(p,"2"));assertThat(stock(p)).isEqualByComparingTo("9");
        ventas.confirmar(v);assertThat(stock(p)).isEqualByComparingTo("7");
        var compra=venta(p,"5");compra.setProveedorId(proveedor);compra.getLineas().get(0).setCostoUnitario(new BigDecimal("1000"));
        long c=compras.crear(compra);compras.confirmar(c);assertThat(stock(p)).isEqualByComparingTo("12");
        ventas.anular(v,"Devolución");assertThat(stock(p)).isEqualByComparingTo("14");
        data.put("unidades_iniciales","0");data.put("stock_minimo","3");productos.guardar(p,data);
        assertThat(stock(p)).isEqualByComparingTo("14");
        mvc.perform(get("/ventas/nueva")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("(stock: 14)")));
        mvc.perform(get("/inventario/disponibles")).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("TEST-MAJO-INIT")));
    }
    @Test void stockMinimoNoSumaYEntradaInicialInvalidaNoCreaProducto() {
        productos.guardar(null,productoInicial("TEST-MAJO-MIN","0"));assertThat(stock(id("producto","codigo","TEST-MAJO-MIN"))).isEqualByComparingTo("0");
        var invalid=productoInicial("TEST-MAJO-BAD","9");invalid.remove("costo_inicial");
        assertThatThrownBy(()->productos.guardar(null,invalid)).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE codigo='TEST-MAJO-BAD'",Integer.class)).isZero();
        invalid.put("costo_inicial","1.000");invalid.put("activo","false");
        assertThatThrownBy(()->productos.guardar(null,invalid)).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE codigo='TEST-MAJO-BAD'",Integer.class)).isZero();
    }
    @Test void retiroConservaStockYProveedorRelacionadoYEliminaSoloSinUso() {
        productos.guardar(null,productoInicial("TEST-MAJO-KEEP","9"));
        productos.guardar(null,productoInicial("TEST-MAJO-REMOVE","0"));
        long keep=id("producto","codigo","TEST-MAJO-KEEP"),remove=id("producto","codigo","TEST-MAJO-REMOVE");
        var plan=productos.revisarRetiro(List.of(keep,remove));
        assertThat(plan).extracting(r->r.get("accion")).containsExactly("DESACTIVAR","ELIMINAR");
        productos.retirar(List.of(keep,remove));
        assertThat(stock(keep)).isEqualByComparingTo("9");
        assertThat(jdbc.queryForObject("SELECT activo FROM producto WHERE id=?",Boolean.class,keep)).isFalse();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM producto WHERE id=?",Integer.class,remove)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario WHERE producto_id=?",Integer.class,keep)).isEqualTo(1);
        var proveedores=catalogos.stream().filter(s->s.definicion().tabla().equals("proveedor")).findFirst().orElseThrow();
        proveedores.retirar(List.of(proveedor));assertThat(jdbc.queryForObject("SELECT activo FROM proveedor WHERE id=?",Boolean.class,proveedor)).isFalse();
        assertThat(jdbc.queryForObject("SELECT proveedor_preferido_id FROM producto WHERE id=?",Long.class,producto)).isEqualTo(proveedor);
        assertThat(productos.listar("Cartulina de prueba","ACTIVOS")).isEmpty();
        assertThat(productos.listar("Cartulina de prueba","INACTIVOS")).hasSize(1);
    }
    @Test void revisionDeRetiroNoModificaHastaConfirmarYProtegePermisos() throws Exception {
        jdbc.update("INSERT INTO proveedor(nombre) VALUES ('Temporal Majo')");long p=id("proveedor","nombre","Temporal Majo");
        mvc.perform(post("/catalogos/proveedores/retirar/revisar").with(csrf()).param("ids",String.valueOf(p)))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Temporal Majo")));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM proveedor WHERE id=?",Integer.class,p)).isEqualTo(1);
        mvc.perform(post("/catalogos/proveedores/retirar").with(csrf()).param("ids",String.valueOf(p))).andExpect(status().isBadRequest());
        mvc.perform(post("/catalogos/proveedores/retirar").with(user("vendedor").roles("VENDEDOR")).with(csrf()).param("ids",String.valueOf(p)).param("confirmado","true")).andExpect(status().isForbidden());
        mvc.perform(post("/catalogos/proveedores/retirar").with(user("admin").roles("ADMINISTRADOR")).with(csrf()).param("ids",String.valueOf(p)).param("confirmado","true")).andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM proveedor WHERE id=?",Integer.class,p)).isZero();
    }
    @Test void cantidadesDeCompraAjustesYMinimosDebenSerEnteros() {
        var f=venta(producto,"1.5");f.setProveedorId(proveedor);f.getLineas().get(0).setCostoUnitario(BigDecimal.TEN);
        assertThatThrownBy(()->compras.crear(f)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entero");
        assertThatThrownBy(()->inventario.ajustar(producto,new BigDecimal("0.5"),"AJUSTE_ENTRADA","Conteo",BigDecimal.TEN))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entero");
        var data=new HashMap<String,String>();data.put("categoria_id",String.valueOf(jdbc.queryForObject("SELECT categoria_id FROM producto WHERE id=?",Long.class,producto)));
        data.put("codigo","PAP-001");data.put("nombre","Resma");data.put("tipo","MERCANCIA");data.put("unidad_medida","RESMA");
        data.put("precio_venta","1.300");data.put("stock_minimo","0.5");data.put("activo","true");
        assertThatThrownBy(()->productos.guardar(producto,data)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entero");
        assertThat(stock(producto)).isEqualByComparingTo("0");
    }
    @Test void importesCopSeInterpretanSinConfundirMilesYCentavos() {
        for(String raw:List.of("1300","1.300","1.300,00")) assertThat(Reglas.importe(raw)).isEqualByComparingTo("1300");
        assertThat(Reglas.importe("1.300,50")).isEqualByComparingTo("1300.50");
        assertThat(Reglas.importe("1300.50")).isEqualByComparingTo("1300.50");
        for(String raw:List.of("-1","1.30.0","1,234","1.234,567","NaN","1e3"))
            assertThatThrownBy(()->Reglas.importe(raw)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void formulariosProcesanImportesConSeparadoresEnTodasLasCapas() throws Exception {
        mvc.perform(post("/compras/crear").with(csrf()).param("proveedorId",String.valueOf(proveedor))
            .param("lineas[0].productoId",String.valueOf(producto)).param("lineas[0].cantidad","2").param("lineas[0].costoUnitario","1.300,50")
            .param("lineas[1].productoId",String.valueOf(otro)).param("lineas[1].cantidad","1").param("lineas[1].costoUnitario","2.000")
            .param("descuento","100").param("metodoPago","EFECTIVO")).andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT total FROM compra",BigDecimal.class)).isEqualByComparingTo("4501");
        mvc.perform(post("/ventas/crear").with(csrf()).param("lineas[0].productoId",String.valueOf(producto))
            .param("lineas[0].cantidad","1").param("descuento","0").param("valorRecibido","1.300").param("metodoPago","EFECTIVO"))
            .andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT cambio FROM venta",BigDecimal.class)).isEqualByComparingTo("1100");
        mvc.perform(post("/inventario/ajustar").with(csrf()).param("productoId",String.valueOf(producto)).param("cantidad","2")
            .param("tipo","AJUSTE_ENTRADA").param("motivo","Conteo").param("costo","1.300,50")).andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT costo_promedio FROM producto WHERE id=?",BigDecimal.class,producto)).isEqualByComparingTo("1300.50");
        mvc.perform(post("/finanzas/gastos/registrar").with(csrf()).param("concepto","Luz").param("valor","1.300")
            .param("fecha",LocalDate.now().toString()).param("metodo_pago","EFECTIVO").param("categoria","ENERGIA"))
            .andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT valor FROM gasto",BigDecimal.class)).isEqualByComparingTo("1300");
        mvc.perform(post("/catalogos/productos/guardar").with(csrf()).param("id",String.valueOf(producto))
            .param("categoria_id",String.valueOf(jdbc.queryForObject("SELECT categoria_id FROM producto WHERE id=?",Long.class,producto)))
            .param("codigo","PAP-001").param("nombre","Resma").param("tipo","MERCANCIA").param("unidad_medida","RESMA")
            .param("precio_venta","2.500,50").param("stock_minimo","2").param("activo","true")).andExpect(status().is3xxRedirection());
        assertThat(jdbc.queryForObject("SELECT precio_venta FROM producto WHERE id=?",BigDecimal.class,producto)).isEqualByComparingTo("2500.50");
    }
    @Test void ventasRechazanFraccionesEnProductosYServicios() throws Exception {
        assertThatThrownBy(()->ventas.crear(venta(producto,"1.5")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entero");
        var f=venta(producto,"0.5");f.getLineas().get(0).setProductoId(null);f.getLineas().get(0).setServicioId(servicio);
        assertThatThrownBy(()->ventas.crear(f)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entero");
        mvc.perform(post("/ventas/crear").with(csrf()).param("lineas[0].productoId",String.valueOf(producto))
            .param("lineas[0].cantidad","1.5").param("metodoPago","EFECTIVO").param("descuento","0").param("valorRecibido","300"))
            .andExpect(status().isBadRequest()).andExpect(content().string(org.hamcrest.Matchers.containsString("entero")));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM venta",Integer.class)).isZero();
    }
    @Test void ventasYComprasMuestranCantidadesEnteras() throws Exception {
        long v=ventas.crear(venta(producto,"2.000"));
        var f=venta(producto,"3");f.setProveedorId(proveedor);f.getLineas().get(0).setCostoUnitario(BigDecimal.TEN);
        long c=compras.crear(f);compras.confirmar(c);assertThat(stock(producto)).isEqualByComparingTo("3");
        mvc.perform(get("/ventas/"+v)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("<td>2</td>")));
        mvc.perform(get("/compras/"+c)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("<td>3</td>")));
        mvc.perform(get("/ventas/nueva")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("min=\"1\" step=\"1\"")));
        mvc.perform(get("/compras/nueva")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("min=\"1\" step=\"1\"")));
    }
    @Test void noConfirmaBorradoresAntiguosConFraccionesNiRedondeaHistorial() throws Exception {
        entrada("3");long v=ventas.crear(venta(producto,"1"));
        jdbc.update("UPDATE detalle_venta SET cantidad=1.5,subtotal=300 WHERE venta_id=?",v);
        jdbc.update("UPDATE venta SET subtotal=300,total=300 WHERE id=?",v);
        assertThatThrownBy(()->ventas.confirmar(v)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("entero");
        assertThat(stock(producto)).isEqualByComparingTo("3");
        assertThat(ventas.obtener(v).getEstado()).isEqualTo("BORRADOR");
        mvc.perform(get("/ventas/"+v)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("<td>1.5</td>")));
    }
    @Test void eliminaRegistrosSinUsoEnLosCuatroCatalogos() throws Exception {
        jdbc.update("INSERT INTO categoria(nombre) VALUES ('Categoria temporal')");
        jdbc.update("INSERT INTO proveedor(nombre) VALUES ('Proveedor temporal')");
        jdbc.update("INSERT INTO producto(categoria_id,codigo,nombre) SELECT categoria_id,'TEMP-DELETE','Producto temporal' FROM producto WHERE id=?",producto);
        jdbc.update("INSERT INTO servicio(codigo,nombre,tipo,precio_unitario) VALUES ('TEMP-DELETE','Servicio temporal','OTRO',100)");
        for(var s:catalogos) {
            var def=s.definicion();
            String nombre=switch(def.tabla()){
                case "categoria" -> "Categoria temporal";
                case "proveedor" -> "Proveedor temporal";
                case "producto" -> "Producto temporal";
                case "servicio" -> "Servicio temporal";
                default -> throw new IllegalStateException();
            };
            long temporal=id(def.tabla(),"nombre",nombre);
            mvc.perform(post("/catalogos/"+def.ruta()+"/"+temporal+"/eliminar").with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/catalogos/"+def.ruta()));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+def.tabla()+" WHERE id=?",Integer.class,temporal)).isZero();
        }
    }
    @Test void eliminarConReferenciasConservaCatalogosEHistorial() throws Exception {
        entrada("1");
        var f=venta(producto,"1");var l=new OperacionForm.Linea();l.setServicioId(servicio);l.setCantidad(BigDecimal.ONE);
        f.setLineas(List.of(f.getLineas().get(0),l));long v=ventas.crear(f);ventas.confirmar(v);
        assertThat(stock(producto)).isEqualByComparingTo("0");
        long categoria=jdbc.queryForObject("SELECT categoria_id FROM producto WHERE id=?",Long.class,producto);
        for(var s:catalogos) {
            var def=s.definicion();long key=switch(def.tabla()){
                case "categoria" -> categoria;
                case "proveedor" -> proveedor;
                case "producto" -> producto;
                case "servicio" -> servicio;
                default -> throw new IllegalStateException();
            };
            mvc.perform(post("/catalogos/"+def.ruta()+"/"+key+"/eliminar").with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("aviso",org.hamcrest.Matchers.containsString("Puedes desactivarlo")));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM "+def.tabla()+" WHERE id=?",Integer.class,key)).isEqualTo(1);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM detalle_venta WHERE venta_id=?",Integer.class,v)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario WHERE producto_id=?",Integer.class,producto)).isEqualTo(2);
    }
    @Test void estadoSePuedeReactivarSinBorrarStockNiHistorial() throws Exception {
        entrada("2");
        assertThatThrownBy(()->productos.eliminar(producto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("existencias");
        String ruta="/catalogos/productos/"+producto+"/estado";
        mvc.perform(post(ruta).with(csrf()).param("activo","false")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/catalogos/productos").param("estado","INACTIVOS")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(">Activar</button>")));
        assertThat(jdbc.queryForObject("SELECT activo FROM producto WHERE id=?",Boolean.class,producto)).isFalse();
        mvc.perform(post(ruta).with(csrf()).param("activo","true")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/catalogos/productos")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(">Desactivar</button>")));
        assertThat(jdbc.queryForObject("SELECT activo FROM producto WHERE id=?",Boolean.class,producto)).isTrue();
        assertThat(stock(producto)).isEqualByComparingTo("2");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario WHERE producto_id=?",Integer.class,producto)).isEqualTo(1);
    }
    @Test @WithMockUser(username="vendedor",roles="VENDEDOR")
    void cambiosDeCatalogoExigenPermisosYCsrf() throws Exception {
        for(String ruta:List.of("/catalogos/productos/"+producto,"/catalogos/servicios/"+servicio)) {
            mvc.perform(post(ruta+"/eliminar").with(csrf())).andExpect(status().isForbidden());
            mvc.perform(post(ruta+"/estado").with(csrf()).param("activo","false")).andExpect(status().isForbidden());
        }
        mvc.perform(post("/catalogos/productos/"+producto+"/eliminar").with(user("admin").roles("ADMINISTRADOR")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/catalogos/productos/"+producto+"/estado").with(user("admin").roles("ADMINISTRADOR")).param("activo","false"))
            .andExpect(status().isForbidden());
    }
    @Test void compraVentaYAnulacionPreservanStockEHistorial() {
        var f=venta(producto,"10");f.setProveedorId(proveedor);f.getLineas().get(0).setCostoUnitario(new BigDecimal("100"));f.setDescuento(new BigDecimal("100"));
        long c=compras.crear(f);assertThat(stock(producto)).isEqualByComparingTo("0");
        compras.confirmar(c);assertThat(stock(producto)).isEqualByComparingTo("10");
        assertThat(jdbc.queryForObject("SELECT costo_promedio FROM producto WHERE id=?",BigDecimal.class,producto)).isEqualByComparingTo("90");
        assertThatThrownBy(()->compras.confirmar(c)).isInstanceOf(IllegalArgumentException.class);
        long v=ventas.crear(venta(producto,"3"));ventas.confirmar(v);assertThat(stock(producto)).isEqualByComparingTo("7");
        assertThat(ventas.obtener(v).getTotal()).isEqualByComparingTo("600");
        assertThat(ventas.obtener(v).getCambio()).isEqualByComparingTo("99400");
        ventas.anular(v,"Error de digitación");assertThat(stock(producto)).isEqualByComparingTo("10");
        assertThatThrownBy(()->ventas.anular(v,"Otra vez")).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario",Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM detalle_venta WHERE venta_id=?",Integer.class,v)).isEqualTo(1);
    }
    @Test void stockInsuficienteRevierteTodaLaTransaccion() {
        entrada("5");var f=venta(producto,"2");var l=new OperacionForm.Linea();l.setProductoId(otro);l.setCantidad(BigDecimal.ONE);f.setLineas(List.of(f.getLineas().get(0),l));
        long id=ventas.crear(f);
        assertThatThrownBy(()->ventas.confirmar(id)).isInstanceOf(IllegalArgumentException.class);
        assertThat(stock(producto)).isEqualByComparingTo("5");
        assertThat(ventas.obtener(id).getEstado()).isEqualTo("BORRADOR");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario WHERE venta_id=?",Integer.class,id)).isZero();
    }
    @Test void anularBorradorNoMueveExistencias() {
        long id=ventas.crear(venta(producto,"1"));ventas.anular(id,"No se completó el pedido");
        assertThat(stock(producto)).isEqualByComparingTo("0");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario",Integer.class)).isZero();
    }
    @Test void ventaMixtaGuardaSnapshotsYNoDescuentaInsumosImplicitamente() {
        entrada("5");var f=venta(producto,"1");var l=new OperacionForm.Linea();l.setServicioId(servicio);l.setCantidad(new BigDecimal("2"));f.setLineas(List.of(f.getLineas().get(0),l));
        long id=ventas.crear(f);ventas.confirmar(id);
        assertThat(ventas.lineas(id)).hasSize(2);assertThat(stock(producto)).isEqualByComparingTo("4");
        assertThat(ventas.obtener(id).getTotal()).isEqualByComparingTo("1000");
        jdbc.update("UPDATE producto SET precio_venta=999 WHERE id=?",producto);
        assertThat(ventas.lineas(id).stream().filter(d->d.getProductoId()!=null).findFirst().orElseThrow().getPrecioUnitario()).isEqualByComparingTo("200");
    }
    @Test void validaPrecisionDescuentosDuplicadosYPagos() {
        assertThatThrownBy(()->ventas.crear(venta(producto,"0"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->ventas.crear(venta(producto,"1.0001"))).isInstanceOf(IllegalArgumentException.class);
        var f=venta(producto,"1");f.setDescuento(new BigDecimal("201"));assertThatThrownBy(()->ventas.crear(f)).isInstanceOf(IllegalArgumentException.class);
        f.setDescuento(BigDecimal.ZERO);f.setValorRecibido(BigDecimal.ONE);assertThatThrownBy(()->ventas.crear(f)).isInstanceOf(IllegalArgumentException.class);
        f.setValorRecibido(new BigDecimal("200"));f.setLineas(List.of(f.getLineas().get(0),f.getLineas().get(0)));assertThatThrownBy(()->ventas.crear(f)).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM venta",Integer.class)).isZero();
    }
    @Test void ajustesNuncaPermitenStockNegativo() {
        entrada("2");
        assertThatThrownBy(()->inventario.ajustar(producto,new BigDecimal("3"),"AJUSTE_SALIDA","Consumo",null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(stock(producto)).isEqualByComparingTo("2");
        assertThatThrownBy(()->inventario.ajustar(producto,BigDecimal.ONE,"AJUSTE_ENTRADA","",BigDecimal.ONE)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void ventasSimultaneasNoSobrevenden() throws Exception {
        entrada("1");long a=ventas.crear(venta(producto,"1")),b=ventas.crear(venta(producto,"1"));
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results=new ArrayList<>();
            for(long v:new long[]{a,b}) results.add(pool.submit(()->{
                var ctx=SecurityContextHolder.createEmptyContext();ctx.setAuthentication(UsernamePasswordAuthenticationToken.authenticated("admin","",List.of()));SecurityContextHolder.setContext(ctx);
                try {start.await();ventas.confirmar(v);return true;} catch(IllegalArgumentException e){return false;} finally {SecurityContextHolder.clearContext();}
            }));
            start.countDown();int confirmed=0;for(var result:results) if(result.get(20,TimeUnit.SECONDS)) confirmed++;
            assertThat(confirmed).isEqualTo(1);assertThat(stock(producto)).isEqualByComparingTo("0");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM movimiento_inventario WHERE tipo='SALIDA_VENTA'",Integer.class)).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
    @Test void finanzasYReportesExcluyenAnulados() {
        var date=LocalDate.now().toString();
        gastos.registrar(Map.of("concepto","Luz","valor","100","fecha",date,"metodo_pago","EFECTIVO","categoria","ENERGIA"));
        capital.registrar(Map.of("origen","Ahorros","valor","500","fecha",date,"metodo","TRANSFERENCIA"));
        inversiones.registrar(Map.of("equipo","Impresora","costo","200","fecha",date,"metodo_pago","EFECTIVO","vida_util_meses","24"));
        var resumen=reportes.resumen(LocalDate.now(),LocalDate.now());
        assertThat((BigDecimal)resumen.get("Flujo neto de operaciones vigentes")).isEqualByComparingTo("200");
        long id=((Number)gastos.listar().get(0).get("id")).longValue();gastos.anular(id,"Duplicado");
        assertThat((BigDecimal)reportes.resumen(LocalDate.now(),LocalDate.now()).get("Gastos")).isEqualByComparingTo("0");
        assertThatThrownBy(()->gastos.anular(id,"Otra vez")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->reportes.resumen(LocalDate.now(),LocalDate.now().minusDays(1))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void catalogoIgnoraStockInyectadoYProtegeConsultas() {
        var data=new HashMap<String,String>();data.put("categoria_id",String.valueOf(jdbc.queryForObject("SELECT categoria_id FROM producto WHERE id=?",Long.class,producto)));
        data.put("codigo","TEST-01");data.put("nombre","Prueba ' OR 1=1 --");data.put("tipo","MERCANCIA");data.put("unidad_medida","UNIDAD");data.put("precio_venta","1200");data.put("stock_minimo","1");data.put("activo","true");data.put("stock_actual","999");
        productos.guardar(null,data);long p=id("producto","codigo","TEST-01");assertThat(stock(p)).isEqualByComparingTo("0");
        assertThat(productos.listar("' OR 1=1 --")).hasSize(1);
        jdbc.update("DELETE FROM producto WHERE id=?",p);
    }
    @Test void todasLasPantallasRenderizanSinErrores() throws Exception {
        long v=ventas.crear(venta(producto,"1"));
        var f=venta(producto,"1");f.setProveedorId(proveedor);f.getLineas().get(0).setCostoUnitario(BigDecimal.TEN);long c=compras.crear(f);
        gastos.registrar(Map.of("concepto","Luz","valor","100","fecha",LocalDate.now().toString(),"metodo_pago","EFECTIVO","categoria","ENERGIA"));
        capital.registrar(Map.of("origen","Ahorros","valor","500","fecha",LocalDate.now().toString(),"metodo","EFECTIVO"));
        inversiones.registrar(Map.of("equipo","Equipo","costo","200","fecha",LocalDate.now().toString(),"metodo_pago","EFECTIVO"));
        for(String path:List.of("/","/login","/catalogos/productos","/catalogos/productos/nuevo","/catalogos/productos/"+producto+"/editar","/catalogos/categorias","/catalogos/categorias/nuevo","/catalogos/proveedores","/catalogos/proveedores/nuevo","/catalogos/servicios","/catalogos/servicios/nuevo","/ventas","/ventas/nueva","/ventas/"+v,"/compras","/compras/nueva","/compras/"+c,"/inventario","/finanzas/gastos","/finanzas/capital","/finanzas/inversiones","/reportes","/usuarios"))
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(get("/webjars/bootstrap/5.3.3/css/bootstrap.min.css")).andExpect(status().isOk());
        mvc.perform(get("/reportes/exportar").param("desde",LocalDate.now().toString()).param("hasta",LocalDate.now().toString())).andExpect(status().isOk()).andExpect(header().string("Content-Disposition","attachment; filename=impresiones-majo-resumen.csv"));
    }
    @Test void formulariosCreanYConfirmanVentasConCsrf() throws Exception {
        entrada("2");
        var result=mvc.perform(post("/ventas/crear").with(csrf()).param("lineas[0].productoId",String.valueOf(producto)).param("lineas[0].cantidad","1").param("metodoPago","EFECTIVO").param("descuento","0").param("valorRecibido","300")).andExpect(status().is3xxRedirection()).andReturn();
        String url=result.getResponse().getRedirectedUrl();
        mvc.perform(post(url+"/confirmar").with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post(url+"/confirmar").with(csrf())).andExpect(status().isBadRequest());
        assertThat(stock(producto)).isEqualByComparingTo("1");
        mvc.perform(post(url+"/anular").with(csrf()).param("motivo","Prueba de devolución")).andExpect(status().is3xxRedirection());assertThat(stock(producto)).isEqualByComparingTo("2");
    }
    @Test @WithMockUser(username="vendedor",roles="VENDEDOR")
    void vendedorNoAccedeAdministracionNiVentasAjenas() throws Exception {
        long propia=ventas.crear(venta(producto,"1"));
        for(String path:List.of("/usuarios","/compras","/finanzas/gastos","/finanzas/capital","/finanzas/inversiones","/reportes","/catalogos/productos/nuevo","/catalogos/proveedores"))
            mvc.perform(get(path)).andExpect(status().isForbidden());
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/catalogos/servicios")).andExpect(status().isOk());
        jdbc.update("INSERT INTO venta(usuario_id) SELECT id FROM usuario WHERE nombre_usuario='admin'");
        long ajena=jdbc.queryForObject("SELECT MAX(id) FROM venta",Long.class);
        mvc.perform(get("/ventas/"+ajena)).andExpect(status().isForbidden());
        mvc.perform(post("/inventario/ajustar").with(csrf()).param("productoId",String.valueOf(producto)).param("cantidad","1").param("tipo","AJUSTE_ENTRADA").param("motivo","Intento").param("costo","10")).andExpect(status().isForbidden());
        mvc.perform(post("/ventas/"+propia+"/anular").with(csrf()).param("motivo","Intento")).andExpect(status().isForbidden());
    }
    @Test void hashesBCryptCsrfYSesionesRevocadas() throws Exception {
        assertThat(hash).startsWith("$2");assertThat(encoder.matches("UnaClaveSegura123!",hash)).isTrue();
        // El login con credenciales válidas/incorrectas se verifica por HTTP real en HttpSmokeTest.
        mvc.perform(post("/ventas/1/confirmar")).andExpect(status().isForbidden());
        mvc.perform(get("/inventario").with(anonymous())).andExpect(status().is3xxRedirection());
        jdbc.update("UPDATE usuario SET activo=0 WHERE nombre_usuario='admin'");
        mvc.perform(get("/usuarios").with(user("admin").roles("ADMINISTRADOR"))).andExpect(status().isForbidden());
    }
    @Test void cambiosSimultaneosConservanUnAdministrador() throws Exception {
        usuarios.crear("segundo","Otro administrador",null,"OtraClaveSegura123!","ADMINISTRADOR");
        long primero=id("usuario","nombre_usuario","admin"),segundo=id("usuario","nombre_usuario","segundo");
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results=new ArrayList<>();
            for(String actor:List.of("admin","segundo")) results.add(pool.submit(()->{
                var ctx=SecurityContextHolder.createEmptyContext();ctx.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(actor,"",List.of()));SecurityContextHolder.setContext(ctx);
                try {start.await();usuarios.actualizar(actor.equals("admin")?segundo:primero,false,"VENDEDOR",null);return true;}
                catch(IllegalArgumentException | org.springframework.security.access.AccessDeniedException e){return false;}
                finally {SecurityContextHolder.clearContext();}
            }));
            start.countDown();int changed=0;for(var result:results) if(result.get(20,TimeUnit.SECONDS)) changed++;
            assertThat(changed).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario u JOIN usuario_rol ur ON ur.usuario_id=u.id JOIN rol r ON r.id=ur.rol_id WHERE u.activo=1 AND r.codigo='ADMINISTRADOR'",Integer.class)).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
    @Test void usuariosNoExponenPasswordYNoPierdenUltimoAdministrador() {
        usuarios.crear("nuevo","Persona",null,"OtraClaveSegura123!","VENDEDOR");
        assertThat(usuarios.listar()).allSatisfy(u->assertThat(u).doesNotContainKeys("password_hash","password"));
        assertThatThrownBy(()->usuarios.actualizar(id("usuario","nombre_usuario","admin"),false,"VENDEDOR",null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->usuarios.crear("corto","Persona",null,"123","VENDEDOR")).isInstanceOf(IllegalArgumentException.class);
        var vo=new com.impresionesmajo.model.vo.UsuarioVO();vo.setPasswordHash(hash);vo.setEmail("privado@example.com");
        assertThat(vo.toString()).doesNotContain(hash,"privado@example.com");
        usuarios.aprovisionar("otroadmin","OtraClaveSegura123!","Admin");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE nombre_usuario='otroadmin'",Integer.class)).isZero();
    }
}
