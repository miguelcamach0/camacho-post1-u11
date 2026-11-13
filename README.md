# Post-contenido Unidad 11 — Refactorización guiada por pruebas
**Asignatura:** Patrones de Diseño de Software  
**Proyecto:** `refactoring-u11`  


## Objetivo

Identificar dos problemas de diseño (*Long Method* y *Switch Statements* duplicados), proteger el comportamiento original con pruebas de caracterización y eliminarlos mediante refactorizaciones verificadas con las mismas pruebas. Todo el trabajo corresponde a la **Parte 1** de la Unidad 11; la Parte 2 se añadirá posteriormente a este mismo repositorio.

## Parte 1 — Red de seguridad y refactorización

### Paso 1. Proyecto y diagnóstico del código original

Se creó un proyecto Spring Boot con Maven, Java 17 y el paquete base `com.universidad.refactoringu11`. Se implementó el paquete `inventario`, con `Producto`, `TipoProducto`, `InventarioService`, `ClasificadorProducto` y `EtiquetadoProducto`.

**Estructura inicial:**

![Estructura inicial del proyecto](docs/parte1/01-estructura-inicial.png)

**Comprobación de compilación:** el código original compila sin errores mediante `mvn compile`.

![Compilación del código original](docs/parte1/01-mvn-compile-success.png)

**Code Smell 1 — Long Method.** El método `InventarioService.actualizarInventario()` contiene cinco grupos de responsabilidades: validar los datos, buscar el producto, calcular y actualizar el stock, calcular el reorden, y registrar el movimiento y emitir alertas. En el código original proporcionado en la guía, el método ocupa **44 líneas físicas** (incluidas la declaración, llave de cierre, líneas vacías y comentarios), de las cuales **38 no están vacías**. Esta medida se refiere al código original de la guía; si el formato del archivo local es diferente, debe prevalecer la medición real.

![Long Method original — primera parte](docs/parte1/01-long-method-original.png)

![Long Method original — continuación](docs/parte1/01-long-method-original-2.png)

**Code Smell 2 — Switch Statements duplicados.** `ClasificadorProducto` y `EtiquetadoProducto` repiten un `switch` sobre los cuatro valores de `TipoProducto` (`LIBRO`, `ELECTRONICO`, `ROPA`, `ALIMENTO`). Hay **2 switches** y **8 ramas `case`** en total. La duplicación obliga a editar ambas clases cuando se incorpora un tipo nuevo.

![Switch original de ClasificadorProducto](docs/parte1/01-switch-clasificador.png)

![Switch original de EtiquetadoProducto](docs/parte1/01-switch-etiquetado.png)

### Paso 2. Pruebas de caracterización antes de refactorizar

Antes de cambiar el código de producción, se crearon pruebas JUnit 5 que ejecutan las clases directamente, sin iniciar el contexto de Spring. Las pruebas documentan entradas, salidas, errores de inventario, cálculo de reorden, recargos y etiquetas.

| Clase | Casos del enunciado | Ampliación recomendada |
|---|---:|---:|
| `InventarioServiceTest` | 6 | 6 |
| `ClasificadorProductoTest` | 4 | 4 |
| `EtiquetadoProductoTest` | 2 | 4, incluyendo ROPA y ALIMENTO |
| **Total** | **12** | **14** |

**Cantidad realmente ejecutada:** [anotar el total que muestra `mvn test`; puede ser mayor si se conservó una prueba automática de Spring Boot].

![Estructura de las clases de prueba](docs/parte1/02-estructura-pruebas.png)

![Código original sin modificar antes de la refactorización](docs/parte1/02-codigo-original-sin-modificar.png)

![Pruebas de caracterización en verde](docs/parte1/02-pruebas-caracterizacion-verde.png)

Estas pruebas se registraron en Git **antes** de realizar cualquiera de las dos refactorizaciones.

### Paso 3. Refactorización del Long Method

**Técnica elegida:** *Extract Method* (extraer método), del catálogo de Martin Fowler.

`actualizarInventario()` queda como método coordinador y delega en `datosValidos`, `buscarProducto` y `procesarMovimiento`. Este último organiza las llamadas a `calcularNuevoStock`, `calcularCantidadReorden`, `registrarMovimiento` y `generarResultado`. Las fórmulas, condiciones de retorno y mensajes del código original se conservaron.

| Indicador | Antes | Después |
|---|---:|---:|
| Líneas físicas de `actualizarInventario()`* | 44 | 6 |
| Grupos de responsabilidades implementados directamente | 5 | Coordinación y delegación |
| Métodos privados de apoyo | 0 | 7 |
| Pruebas originales modificadas | — | 0 |

\*Criterio: incluir declaración, llave de cierre, comentarios y líneas vacías. El valor inicial de 44 corresponde al código transcrito de la guía; confirmar si el archivo inicial fue formateado de manera distinta.

![Método actualizarInventario refactorizado](docs/parte1/03-metodo-actualizarInventario-refactorizado.png)

![Pruebas verdes después de refactorizar el método](docs/parte1/03-pruebas-en-verde-después-de-refactorizar-el-metodo.png)

#### Decisión de diseño 1 — ¿Método privado o clase CalculadoraReorden?

**Se eligió mantener `calcularCantidadReorden()` como método privado de `InventarioService`.** La fórmula depende únicamente del stock disponible y del stock mínimo, es simple y no utiliza dependencias externas. En el dominio actual no existen distintas políticas de reorden que justifiquen una clase colaboradora independiente. Extraer una clase ahora aumentaría la cantidad de componentes sin un beneficio proporcional. Si aparecen reglas distintas por proveedor, categoría o temporada, será oportuno crear una clase especializada.

### Paso 4. Eliminación del Switch Statement duplicado

**Técnica elegida:** *Replace Conditional with Polymorphism* (reemplazar condicional con polimorfismo).

Los dos `switch` se sustituyeron por comportamiento polimórfico en `TipoProducto`: cada constante del `enum` implementa `calcularRecargoEnvio(double)` y `generarEtiqueta(String)`. Las clases `ClasificadorProducto` y `EtiquetadoProducto` delegan en el `enum`, **sin modificar las firmas públicas originales**.

**Antes de la refactorización:**

![TipoProducto antes de refactorizar](docs/parte1/04-TipoProducto-antes-refactorizar.png)

![ClasificadorProducto con switch original](docs/parte1/04-ClasificadorProducto-sin-refactorizar-original.png)

![EtiquetadoProducto con switch original](docs/parte1/04-EtiquetadoProducto-con-switch-sin-refactorizar.png)

**Después de la refactorización:**

![Enum polimórfico — primera parte](docs/parte1/04-enum-polimorfismo-refactorizado.png)

![Enum polimórfico — segunda parte](docs/parte1/04-enum-polimorfismo-refactorizado-segundaparte.png)

![ClasificadorProducto sin switch](docs/parte1/04-ClasificadorProducto-refactorizado-sin-switch.png)

![EtiquetadoProducto sin switch](docs/parte1/04-EtiquetadoProducto-refactorizado.png)

| Indicador | Antes | Después |
|---|---:|---:|
| Switches sobre `TipoProducto` en las dos clases | 2 | 0 |
| Ramas `case` en esos switches | 8 | 0 |
| Cambios necesarios en `ClasificadorProducto` y `EtiquetadoProducto` al añadir un tipo | 2 clases | 0 clases |
| Firmas públicas conservadas | 2 | 2 |

**Evidencia de regresión:**

![Pruebas verdes después de aplicar polimorfismo](docs/parte1/04-pruebas-polimorfismo-verde.png)

#### Decisión de diseño 2 — ¿Enum polimórfico o interfaz Strategy?

**Se eligió un `enum` con métodos abstractos implementados por cada constante.** Los cuatro tipos forman un conjunto cerrado y estable; sus reglas de recargo y etiquetado son sencillas y no necesitan servicios externos. Esta alternativa evita crear una interfaz y varias clases de estrategia, y permite añadir un tipo sin modificar las clases cliente. Si cada tipo necesitara dependencias inyectadas por Spring, configuración dinámica o algoritmos más complejos, sería preferible utilizar estrategias independientes.

### Paso 5. Consolidación de la evidencia y primer push

Las capturas se encuentran en `docs/parte1/` y están insertadas en el README **inmediatamente después de la explicación que respaldan**. El historial de commits permite verificar el orden de trabajo: código original, pruebas de caracterización, refactorización del Long Method, refactorización de los switches y documentación.

**Resultados de Maven por etapa:**

| Momento | Evidencia | Resultado |
|---|---|---|
| Código original | `01-mvn-compile-success.png` | `BUILD SUCCESS` en `mvn compile` |
| Pruebas antes de refactorizar | `02-pruebas-caracterizacion-verde.png` | Pruebas verdes; registrar cantidad exacta de Maven |
| Después de extraer métodos | `03-pruebas-en-verde-después-de-refactorizar-el-metodo.png` | Pruebas verdes; misma batería de caracterización |
| Después del polimorfismo | `04-pruebas-polimorfismo-verde.png` | Pruebas verdes; misma batería de caracterización |

Para verificar el estado local del proyecto:

```bash
mvn test
git log --oneline -5
git status
```



> **Pendiente:** Parte 2 — Diagnóstico de dependencias cíclicas entre paquetes, ArchUnit e inversión de dependencias. Se completará en este mismo `README.md`, sin crear otro repositorio.


