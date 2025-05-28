# 📊 Reading Service

Microservicio para el monitoreo en tiempo real de lecturas de glucosa de bombas de insulina.

## 🚀 Descripción

El Reading Service es el núcleo del monitoreo médico, responsable de procesar, clasificar y analizar todas las lecturas de glucosa de los pacientes, generando alertas automáticas y estadísticas médicas.

## 🛠️ Tecnologías

- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Data JPA**
- **MySQL 8.0**
- **Spring Cloud Netflix Eureka**
- **OpenFeign**
- **Lombok**
- **Bean Validation**

## 📋 Funcionalidades

### ✅ Monitoreo de Glucosa
- Registro de lecturas en tiempo real
- Clasificación automática de estados
- Alertas para valores críticos
- Historial completo de lecturas

### ✅ Estados Automáticos
- **NORMAL**: 70-180 mg/dL
- **LOW**: 50-70 mg/dL
- **HIGH**: 180-250 mg/dL
- **CRITICAL_LOW**: < 50 mg/dL ⚠️ **Requiere Acción**
- **CRITICAL_HIGH**: > 250 mg/dL ⚠️ **Requiere Acción**

### ✅ Análisis y Estadísticas
- Promedios de glucosa por período
- Desviación estándar
- Conteo de lecturas problemáticas
- Tendencias y patrones

## 🌐 Endpoints Principales

### Acceso Directo (Puerto 8083)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/readings` | Obtener todas las lecturas |
| GET | `/api/readings/{id}` | Obtener lectura por ID |
| GET | `/api/readings/device/{deviceId}` | Lecturas de un dispositivo |
| POST | `/api/readings` | Crear nueva lectura |
| PUT | `/api/readings/{id}` | Actualizar lectura |
| DELETE | `/api/readings/{id}` | Eliminar lectura |

### Acceso a través de Gateway (Puerto 8087) - **RECOMENDADO**
| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| GET | `http://localhost:8087/api/readings` | Obtener todas las lecturas |
| GET | `http://localhost:8087/api/readings/{id}` | Obtener lectura por ID |
| GET | `http://localhost:8087/api/readings/device/{deviceId}` | Lecturas de un dispositivo |
| POST | `http://localhost:8087/api/readings` | Crear nueva lectura |

## 🔍 Consultas Especializadas

### Acceso a través de Gateway
| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| GET | `http://localhost:8087/api/readings/patient/{patientId}` | Lecturas de un paciente |
| GET | `http://localhost:8087/api/readings/device/{deviceId}/latest` | Última lectura |
| GET | `http://localhost:8087/api/readings/device/{deviceId}/timerange` | Por rango de tiempo |
| GET | `http://localhost:8087/api/readings/status/{status}` | Filtrar por estado |
| GET | `http://localhost:8087/api/readings/requiring-action` | ⚠️ **Lecturas críticas** |

## 📈 Estadísticas y Analytics

### Acceso a través de Gateway
| Método | Endpoint Gateway | Descripción |
|--------|------------------|-------------|
| GET | `http://localhost:8087/api/readings/device/{deviceId}/statistics` | Estadísticas completas |

**Parámetros:**
- `start`: Fecha inicio (ISO format)
- `end`: Fecha fin (ISO format)

**Respuesta incluye:**
- Promedio de glucosa
- Lecturas altas/bajas
- Desviación estándar
- Total de lecturas

## 🗄️ Modelo de Datos

```java
@Entity
public class Reading {
    private Long id;
    private Float glucoseLevel;        // mg/dL
    private LocalDateTime timestamp;
    private Long deviceId;
    private ReadingStatus status;      // Auto-calculado
    private String notes;
    private Float insulinDose;         // Unidades
    private Float carbIntake;          // Gramos
    private Boolean manualReading;
    private Boolean requiresAction;    // Auto-calculado
}
```

## ⚙️ Configuración

### Puerto
```
server.port=8083
```

### Base de Datos
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lecturas
spring.datasource.username=root
spring.datasource.password=****
```

### Eureka
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## 🚀 Ejecución

1. **Iniciar MySQL** en puerto 3306
2. **Iniciar Eureka Server** en puerto 8761
3. **Iniciar Gateway Service** en puerto 8087
4. **Iniciar Patient Service** en puerto 8081
5. **Iniciar Device Service** en puerto 8082
6. **Ejecutar la aplicación**
   
7. **Verificar:**
   - Directo: http://localhost:8083/api/readings
   - **Gateway**: http://localhost:8087/api/readings



## 🔗 Comunicación con Otros Servicios

### Device Service
- Valida existencia de dispositivos
- Obtiene información técnica de dispositivos

### Patient Service
- Obtiene información de pacientes
- Valida pacientes para lecturas

### Gateway Service
- Enrutamiento automático de peticiones
- Balanceador de carga
- CORS y logging centralizado

## 💡 Características Especiales

### Lógica Médica Automática
```java
// Determinación automática de estado
if (glucoseLevel < 50) → CRITICAL_LOW + requiresAction = true
if (glucoseLevel > 250) → CRITICAL_HIGH + requiresAction = true
```

### Cálculos Estadísticos
- Promedio de glucosa
- Desviación estándar
- Percentiles
- Conteos por categoría

### Alertas en Tiempo Real
- Identificación automática de emergencias
- Marcado de lecturas que requieren acción
- Notificaciones para personal médico

## 🚨 Casos de Uso Críticos

### Emergencias Médicas
1. **Hipoglucemia Severa** (< 50 mg/dL)
2. **Hiperglucemia Crítica** (> 250 mg/dL)
3. **Tendencias peligrosas**
4. **Fallas de dispositivo**

### Monitoreo Rutinario
1. **Seguimiento diario**
2. **Análisis de patrones**
3. **Ajuste de tratamientos**
4. **Reportes médicos**

## 👨‍💻 Desarrollador

**Rafael Gamero Arrabal**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/rafael-gamero-arrabal-619200186/)

---

⭐ **Parte del Sistema de Microservicios para Bombas de Insulina**
