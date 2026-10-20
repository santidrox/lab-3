# TecniHogar - Laboratorio 3 de Herencia

Aplicación de consola para cotizar servicios, registrar órdenes y cuadrillas, administrar jornadas, registrar visitas y consultar ingresos. Usa herencia, clases abstractas, encapsulamiento y polimorfismo. No requiere librerías externas, archivos de datos ni base de datos.

## Requisitos

JDK 17 o superior. Comprueba `java -version` y `javac -version`. Abre una terminal dentro de la carpeta TecniHogar (donde se encuentra este README).

## Compilar y ejecutar

Los comandos funcionan en PowerShell, CMD y terminales de Linux/macOS:

```text
javac -encoding UTF-8 -d bin src/*.java
java -cp bin Main
```

Si bin no existe, créala con `mkdir bin`. Si los acentos se ven mal en CMD, ejecuta `chcp 65001` antes de iniciar. Para decimales escribe punto, por ejemplo `2.5`. Las respuestas de confirmación son `s` y `n`. Un dato inválido rechaza la operación y regresa al menú.

## Pruebas reproducibles

Desde la misma carpeta:

```text
javac -encoding UTF-8 -d bin src/*.java test/*.java
java -cp bin Pruebas
```

La clase Pruebas comprueba 133 condiciones y genera `docs/resultados-pruebas.tsv` y muestra su salida. El archivo `docs/salida-pruebas.txt` conserva la ejecución realizada al preparar la entrega. `docs/prueba-consola.txt` contiene una sesión real con entradas inválidas y recuperación del menú. Pruebas usa un sistema independiente del de Main y lanza AssertionError si encuentra una diferencia.

## Estructura

- `src/`: clases del programa, separadas por archivo.
- `test/`: comprobaciones automáticas sin dependencias externas.
- `bin/`: destino de compilación; su contenido no se sube a Git.
- `docs/`: análisis, UML integrado, diagramas editables y evidencia de pruebas.

No se necesitan carpetas lib ni data: no hay dependencias ni persistencia. Al salir se pierden las operaciones de esa sesión.

## Datos iniciales

| Código | Categoría | Zona | Horas | Datos extra | Costo | Días |
| --- | --- | --- | --- | --- | --- | --- |
| D01 | Diagnóstico | Metropolitana | 1 | — | Q150.00 | 1 |
| D02 | Diagnóstico | Periférica | 1.5 | — | Q225.00 | 2 |
| I01 | Instalación | Metropolitana | 2.5 | 5 puntos | Q460.00 | 3 |
| I02 | Instalación | Periférica | 1.2 | 1 punto | Q355.00 | 5 |
| E01 | Eléctrica certificada | Metropolitana | 2.5 | 5 puntos, Q2000 de materiales | Q680.00 | 5 |
| E02 | Eléctrica certificada | Periférica | 2 | 2 puntos, Q1000 de materiales | Q525.00 | 7 |
| A01 | Aire acondicionado | Metropolitana | 1.5 | 9000 BTU | Q345.00 | 2 |
| A02 | Aire acondicionado | Metropolitana | 2 | 60000 BTU | Q400.00 | 2 |

Todas empiezan pendientes, sin visitas y sin ingresos registrados. Los costos de la tabla son cotizaciones; solo las órdenes registradas por el menú cobran su costo. C01 tiene 8 horas y certificación, C02 tiene 6 horas sin certificación y C03 tiene 4 horas con certificación. Las zonas se ingresan explícitamente; el programa no las deduce de la dirección.

## Recorrido de ejemplo

1. Opción 11: comprobar ingresos Q0.00.
2. Opción 4: crear J1 para C01.
3. Opción 5: asignar E01 a J1. Usa 2.5 horas de capacidad aunque facture 4.
4. Opción 7: iniciar J1. Primera visita sin recargo.
5. Opción 8: J1, E01, n. La orden vuelve a pendiente y J1 se cierra.
6. Crear J2 para C01, asignarle E01 y empezar J2. Se registran Q40.00.
7. Registrar otro fallo: E01 queda cancelada por llegar a dos visitas.
8. Opción 11: ingresos por registro Q0.00 y por visitas Q40.00.

## Decisiones

`OrdenServicio` concentra los datos y estados comunes. Cada categoría calcula su costo, plazo y límite de visitas. `MantenimientoAire` decide si una cuadrilla es compatible. `InstalacionElectricaCertificada` extiende `Instalacion` y reutiliza su cálculo con super. No se usa instanceof ni se comprueba el tipo de un objeto para cobrar o programar.

Los importes usan BigDecimal y se registran a dos decimales con HALF_UP. La capacidad se suma con BigDecimal a partir de las horas ingresadas para evitar que 0.1 + 0.2 rechace una capacidad de 0.3. Se valida antes de modificar. Los métodos de transición tienen visibilidad de paquete y se usan desde Jornada; las operaciones del menú pasan por Sistema. Las colecciones expuestas no permiten añadir o retirar elementos.

Interpretaciones: el límite de visitas solo cancela si la última visita falla; si se completa, queda completada. Una orden fallida puede asignarse a otra jornada aunque la jornada anterior todavía espere otros resultados, siempre que la nueva cuadrilla esté libre. Los códigos son únicos por entidad y distinguen mayúsculas; se eliminan espacios al inicio y final. Un retiro no consume visitas. No se reintegra el cobro de registro al cancelar porque el enunciado no solicita devoluciones.

## Publicar en GitHub

Crea en GitHub un repositorio público llamado `TecniHogar-Lab3`, sin README ni .gitignore iniciales. Dentro de esta carpeta ejecuta:

```text
git init
git add .
git commit -m "Implementa laboratorio 3 de herencia TecniHogar"
git branch -M main
git remote add origin https://github.com/santidrox/TecniHogar-Lab3.git
git push -u origin main
```

El comando remoto supone que usas la cuenta santidrox y ese nombre de repositorio; cambia la URL si corresponde. La creación del repositorio y el push requieren tu sesión de GitHub. La URL de arriba es el destino sugerido, no confirma que esté publicado.

## Entrega en Canvas

Adjunta `docs/Lab3-TecniHogar.pdf` y pega la URL del repositorio público después de publicarlo. El PDF incluye análisis, UML y pruebas. Mantén los archivos Java y este README en GitHub.
