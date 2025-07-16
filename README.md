# 🎯 Versionado del Plugin

Este proyecto utiliza **versionado semántico extendido** para representar claramente la evolución del plugin y su compatibilidad con versiones de Minecraft.

## 🧩 Esquema de versión

    MAJOR.MINOR.PATCH[-mcVERSION][+BUILD][-SNAPSHOT]


### 🧱 Componentes

| Parte           | Descripción |
|------------------|-------------|
| `MAJOR`          | Cambios incompatibles con versiones anteriores. Cambios en la API, reestructuraciones, etc. |
| `MINOR`          | Nuevas funcionalidades retrocompatibles. |
| `PATCH`          | Correcciones de errores y mejoras menores. |
| `-mcVERSION`     | (Opcional) Versión de Minecraft soportada. Ej: `-1.20.4` |
| `+BUILD`         | (Opcional) Número de compilación o hash corto del commit. Ej: `+34`, `+g1a2b3c` |
| `-SNAPSHOT`      | (Opcional) Indica una versión en desarrollo. No es estable. |

---

## 🧪 Ejemplos

| Versión | Significado |
|---------|-------------|
| `1.0.0` | Primer release estable. |
| `1.1.0-1.20.4` | Se agregaron nuevas funciones compatibles con Minecraft 1.20.4. |
| `2.0.0-1.21.0` | Reescritura o cambio estructural, ya no compatible con la API anterior. |
| `2.1.3-1.21.0+45` | Tercer parche de la rama 2.1, build #45. |
| `2.2.0-1.21.0-SNAPSHOT` | Versión en desarrollo de la futura 2.2.0. |

---

## 📝 Convenciones de Commit

Este repositorio utiliza [Conventional Commits](https://www.conventionalcommits.org/) para determinar automáticamente el tipo de release a realizar:

| Tipo     | Resultado de versión |
|----------|----------------------|
| `fix:`   | Incrementa `PATCH` |
| `feat:`  | Incrementa `MINOR` |
| `BREAKING CHANGE` o `!` | Incrementa `MAJOR` |

Ejemplo de commit:

```bash
feat: agregar barra de stamina
```

o

```bash
refactor!: eliminar compatibilidad con X plugin

BREAKING CHANGE: ahora se requiere X plugin y Java 17.
```