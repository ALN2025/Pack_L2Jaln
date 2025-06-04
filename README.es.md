# L2Jaln - Emulador de Servidor Lineage II

[![Licencia](https://img.shields.io/badge/Licencia-GPL%20v3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-naranja.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Estado](https://img.shields.io/badge/estado-Activo-verde.svg)]()
[![UTF-8](https://img.shields.io/badge/Encoding-UTF8-azul.svg)]()

## Descripción General

L2Jaln es un emulador de servidor Lineage II construido sobre:
- Código fuente limpio de ACIS (source original limpia)
- Mods seleccionados de la comunidad L2J/Brasil
- Modificaciones desarrolladas personalmente

Esta combinación proporciona un entorno de servidor Lineage II estable y rico en características.

## Características Principales

- **Base Limpia**: Construida sobre código fuente limpio de ACIS
- **Arquitectura Moderna**: Estructura de paquetes actualizada y organización de código optimizada
- **Mods de la Comunidad**: Mejoras seleccionadas de la comunidad L2J/Brasil
- **Características Personalizadas**: Modificaciones e implementaciones propias
- **Rendimiento Mejorado**: Optimización del rendimiento y utilización de recursos
- **Documentación Integral**: Guías detalladas de configuración e instalación

## Sistema de Logging Profesional

- Handlers personalizados para logs de error
- Sistema de log de items
- Log de chat y comandos
- Auditoría de acciones GM
- Logs de PvP y eventos
- Sistema de debug avanzado

## Requisitos Técnicos

- Java Development Kit (JDK) 17 o superior
- Servidor MySQL 8.0 o superior
- Mínimo 4GB de RAM (8GB recomendado)
- Sistema operativo Windows/Linux
- Soporte UTF-8 para caracteres especiales

## Estructura del Proyecto

```
L2Jaln/
├── java/                 # Implementación principal del servidor
│   ├── com.l2jaln/      # Paquete principal
│   │   ├── gameserver/  # Componentes del servidor de juego
│   │   ├── loginserver/ # Componentes del servidor de login
│   │   └── util/        # Clases utilitarias
├── sql/                  # Scripts de base de datos
├── config/              # Archivos de configuración
└── tools/               # Herramientas de gestión del servidor
```

## Instalación

1. Clone el repositorio
2. Importe la base de datos usando los scripts SQL proporcionados
3. Configure los ajustes del servidor en `config/l2jaln.ini`
4. Inicie el servidor de login y el servidor de juego usando los scripts proporcionados

## Cómo Compilar y Ejecutar

1. Compile el proyecto:
   ```sh
   ant dist-local
   ```

2. Inicie los servidores (en terminales separados):
   ```sh
   cd "Pack L2Jaln/login"
   startLoginServer.bat
   ```
   ```sh
   cd "Pack L2Jaln/game"
   startGameServer.bat
   ```

> **Consejo:** Use `chcp 65001` en la terminal para mejor soporte de caracteres especiales.

## Configuración

El servidor puede ser configurado a través de los siguientes archivos:
- `config/l2jaln.ini`: Configuración principal del servidor
- `config/rates.properties`: Tasas y multiplicadores del juego
- `config/network.properties`: Configuraciones de red

## Contribuciones de la Comunidad

Este proyecto ha sido enriquecido con varias modificaciones y mejoras de la comunidad, incluyendo:
- Sistema PvP mejorado
- Eventos y características personalizadas
- Parches de rendimiento optimizados
- Mejoras de seguridad
- Correcciones de bugs y mejoras de estabilidad

## Tecnologías Utilizadas

- Java 17+
- Apache Ant
- UTF-8
- Shell Script & Batch Script
- Sistema de Logging Personalizado

## Licencia

Este proyecto está licenciado bajo la GNU General Public License v3.0 - vea el archivo [LICENSE](LICENSE) para más detalles.

## Agradecimientos

- Proyecto ACIS por proporcionar el código fuente limpio
- Comunidad L2J/Brasil por los mods seleccionados
- Agradecimiento especial a Anderson Luis do Nascimento (A.L.N) por:
  - Desarrollo y adaptación completa
  - Modificaciones y mejoras personalizadas
  - Correcciones de bugs y optimizaciones
  - Mantenimiento y actualizaciones del sistema

> Nota: El código fuente limpio de ACIS usado como base para este proyecto puede no estar disponible para descarga pública.

## Sobre el Proyecto

Sistema completo, moderno y optimizado para servidores privados de Lineage 2, desarrollado en Java.
Basado en aCis, ofrece estabilidad, rendimiento y características avanzadas para administradores y jugadores.

## Repositorio

> **Privado** – uso exclusivo del autor
> GitHub: [ALN2025](https://github.com/ALN2025)

## Firma

```
Trabajo desarrollado y perteneciente a Anderson Luis do Nascimento  
Dev ⩿ A.L.N/⪀
```

---

L2jALN — ¡La mejor tecnología Java para servidores Lineage 2! 