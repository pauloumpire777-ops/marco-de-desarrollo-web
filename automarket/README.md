# AutoMarket Express (Spring Boot + Thymeleaf, backend dummy en memoria)

## Ejecutar
Requisitos: JDK 17+ y Maven binary zip (https://maven.apache.org/download.cgi).

    mvn spring-boot:run        # http://localhost:8080  (redirige a /login)

Los datos viven en listas Java en memoria: se reinician al reiniciar la aplicación.

## Cuentas de demostración (clave `1234`)
| ID | Rol | Nota |
|---|---|---|
| SUP-0001 | Supervisor | Acceso total (incluye Personal y Analítica) |
| EMP-0001 | Cajero | No ve Personal ni Analítica |
| EMP-0003 | Cajero | Suspendido: el login se rechaza |

## Estructura
    pe.automarket
    ├── AutoMarketApplication
    ├── config/      AuthInterceptor (sesión + roles), WebConfig
    ├── controller/  Auth, Pagina (main, contacto, publicidad), Categoria, Producto, Usuario, Caja, Metricas
    ├── service/     InventarioService, UsuarioService, VentaService, MetricasService (listas en memoria)
    ├── model/       Categoria, Producto, Usuario, Carrito, ItemCarrito, Venta, ItemVenta
    └── exception/   ReglaNegocioException, GlobalExceptionHandler
    resources/templates: fragments/layout (head, navbar, alertas, footer, scripts) + una vista por pantalla
    resources/static/css/app.css: estilos compartidos (paleta y tipografía originales)
    Thymeleaf configurado en application.properties (prefix/suffix/modo/encoding/cache)

## Reglas de negocio
| Código | Regla | Dónde se aplica |
|---|---|---|
| RN-01 | Las categorías no se eliminan: solo se desactivan | `InventarioService.desactivarCategoria` (no existe borrado físico) |
| RN-02 | Desactivar una categoría desactiva sus productos; un producto inactivo o de categoría inactiva no se vende ni se reactiva | `InventarioService`, `VentaService.agregar/confirmar` |
| RN-03 | Al confirmar una venta se descuenta el stock | `VentaService.confirmar` |
| RN-04 | No se puede vender más que el stock disponible (considera lo ya agregado al carrito) | `VentaService.agregar/confirmar` |
| RN-05 | Un colaborador suspendido no inicia sesión; el bloqueo también corta la sesión activa | `UsuarioService.autenticar`, `AuthInterceptor` |
| RN-06 | Solo el Supervisor accede a Personal y Analítica | `AuthInterceptor` |
| RN-07 | Nadie puede bloquear su propio acceso | `UsuarioService.bloquear` |

## Funcionalidades (30)
Acceso: 1 login, 2 logout, 3 panel con indicadores, 4 ticket de soporte, 5 promociones.
Categorías: 6 listar, 7 crear, 8 editar, 9 desactivar, 10 reactivar.
Productos: 11 listar con búsqueda y filtro, 12 crear, 13 editar, 14 desactivar, 15 reactivar.
Personal: 16 listar, 17 crear (ID automático), 18 editar, 19 bloquear, 20 desbloquear.
Caja: 21 agregar por código, 22 búsqueda manual, 23 sumar/restar/quitar/vaciar, 24 pagar (3 métodos), 25 boleta.
Analítica: 26 ventas por mes, 27 afluencia por día, 28 top 5 productos, 29 tiempo por transacción, 30 categorías populares.

## Guion de demo sugerido
1. Login con SUP-0001; mostrar EMP-0003 rechazado (RN-05) y EMP-0001 sin acceso a Personal (RN-06).
2. Categorías: desactivar "Bebidas" y ver que sus productos pasan a Oculto (RN-01/02); reactivar.
3. Caja: código 7750001 x2, 4901005 x1; intentar 4901005 x20 (RN-04); pagar y ver boleta.
4. Inventario: el stock bajó (RN-03).
5. Analítica: los gráficos reflejan la venta hecha.
