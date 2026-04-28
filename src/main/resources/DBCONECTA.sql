CREATE DATABASE db_conecta;
USE db_conecta;

-- 1. TABLAS GEOGRÁFICAS
CREATE TABLE departamento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE provincia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    departamento_id INT NOT NULL,
    FOREIGN KEY (departamento_id) REFERENCES departamento(id)
);

CREATE TABLE distrito (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    provincia_id INT NOT NULL,
    FOREIGN KEY (provincia_id) REFERENCES provincia(id)
);

-- 2. SEGURIDAD Y ROLES
CREATE TABLE permission (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(20) NOT NULL
);

CREATE TABLE role_permission (
    role_id INT,
    permission_id INT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id)
);

-- 3. HERENCIA: PERSONA, CLIENTE Y EMPLEADO
CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_persona VARCHAR(20) NOT NULL,
    tipo_documento VARCHAR(10) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50),
    correo VARCHAR(100),
    celular VARCHAR(15),
    fecha_nacimiento DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE cliente (
    id INT PRIMARY KEY,
    estado VARCHAR(20) DEFAULT 'PRECLIENTE',
    FOREIGN KEY (id) REFERENCES persona(id) ON DELETE CASCADE
);

CREATE TABLE empleado (
    id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    account_no_expired BOOLEAN DEFAULT TRUE,
    account_no_locked BOOLEAN DEFAULT TRUE,
    credential_no_expired BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id) REFERENCES persona(id) ON DELETE CASCADE
);

CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES empleado(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id)
);

-- 4. DIRECCIONES
CREATE TABLE direccion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    distrito_id INT NOT NULL,
    tipo_via VARCHAR(30) NOT NULL,
    nombre_via VARCHAR(100),
    numero VARCHAR(20),
    tipo_urbanizacion VARCHAR(30),
    nombre_urbanizacion VARCHAR(100),
    manzana VARCHAR(20),
    lote VARCHAR(20),
    piso VARCHAR(20),
    interior VARCHAR(20),
    tipo_vivienda VARCHAR(30),
    direccion_completa VARCHAR(255) NOT NULL,
    referencia TEXT,
    latitud DECIMAL(10,7) NULL,
    longitud DECIMAL(10,7) NULL,
    is_principal BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    FOREIGN KEY (distrito_id) REFERENCES distrito(id)
);

-- 5. CATÁLOGO COMERCIAL
CREATE TABLE servicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE plan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    servicio_id INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    velocidad_base_mbps INT, 
    precio DECIMAL(10,2) NOT NULL,
    detalle VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (servicio_id) REFERENCES servicio(id)
);
	
CREATE TABLE promocion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATE,
    fecha_fin DATE
);

CREATE TABLE efecto_promocion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    promocion_id INT NOT NULL,
    tipo_efecto VARCHAR(50) NOT NULL, -- Antes ENUM
    valor DECIMAL(10,2) NOT NULL,
    duracion_meses INT NOT NULL,
    FOREIGN KEY (promocion_id) REFERENCES promocion(id) ON DELETE CASCADE
);

CREATE TABLE plan_promocion (
    plan_id INT NOT NULL,
    promocion_id INT NOT NULL,
    PRIMARY KEY (plan_id, promocion_id),
    FOREIGN KEY (plan_id) REFERENCES plan(id) ON DELETE CASCADE,
    FOREIGN KEY (promocion_id) REFERENCES promocion(id) ON DELETE CASCADE
);

-- 6. GESTIÓN ADMINISTRATIVA: CONTRATOS, RECIBOS Y SUSPENSIONES
CREATE TABLE ciclo_pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    dia_emision TINYINT NOT NULL, 
    dia_notificacion TINYINT NOT NULL,
    dia_vencimiento TINYINT NOT NULL, 
    dia_corte TINYINT NOT NULL 
);

-- Ciclo por defecto
INSERT INTO ciclo_pago (nombre, dia_emision, dia_notificacion, dia_vencimiento, dia_corte) 
VALUES ('Ciclo 15', 15, 19, 3, 4);

CREATE TABLE contrato (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    direccion_id INT NOT NULL, 
    plan_id INT NOT NULL,
    promocion_id INT NULL, 
    fecha_fin_promocion DATE, 
    ciclo_pago_id INT NOT NULL,
    empleado_registro_id INT NOT NULL,
    fecha_contrato DATE NOT NULL,
    fecha_activacion DATE, 
    costo_instalacion DECIMAL(10,2) DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'PENDIENTE', -- PENDIENTE, ACTIVO, SUSPENDIDO, CANCELADO
    texto_contrato TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    FOREIGN KEY (direccion_id) REFERENCES direccion(id),
    FOREIGN KEY (plan_id) REFERENCES plan(id),
    FOREIGN KEY (promocion_id) REFERENCES promocion(id),
    FOREIGN KEY (ciclo_pago_id) REFERENCES ciclo_pago(id),
    FOREIGN KEY (empleado_registro_id) REFERENCES empleado(id)
);

CREATE TABLE instalacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    tecnico_id INT NULL,           
    fecha_programada DATE NOT NULL,
    franja_horaria VARCHAR(20) NOT NULL, 
    bloque_asignado VARCHAR(50), -- Columna agregada después de franja_horaria
    estado VARCHAR(20) DEFAULT 'PENDIENTE', 
    observaciones TEXT,            
    FOREIGN KEY (contrato_id) REFERENCES contrato(id),
    FOREIGN KEY (tecnico_id) REFERENCES empleado(id)
);

-- ==============================================================================
-- INICIO DEL MÓDULO DE FACTURACIÓN (CABECERA, DETALLE Y BILLETERA)
-- ==============================================================================

CREATE TABLE recibo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    fecha_emision DATE NOT NULL,      
    fecha_vencimiento DATE NOT NULL,  
    periodo_inicio DATE NOT NULL,     
    periodo_fin DATE NOT NULL,        
    monto_total DECIMAL(10,2) NOT NULL, 
    estado_pago VARCHAR(20) DEFAULT 'PENDIENTE', -- PENDIENTE, PAGADO, VENCIDO, ANULADO
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contrato_id) REFERENCES contrato(id)
);

CREATE TABLE detalle_recibo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recibo_id INT NOT NULL,
    concepto VARCHAR(100) NOT NULL,    
    cantidad_dias INT NULL,            
    precio_unitario DECIMAL(10,2) NOT NULL, 
    subtotal DECIMAL(10,2) NOT NULL,   
    FOREIGN KEY (recibo_id) REFERENCES recibo(id) ON DELETE CASCADE
);

CREATE TABLE saldo_favor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    monto_original DECIMAL(10,2) NOT NULL,   
    monto_disponible DECIMAL(10,2) NOT NULL, 
    origen VARCHAR(50) NOT NULL,             
    estado VARCHAR(20) DEFAULT 'ACTIVO',     -- ACTIVO o CONSUMIDO
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contrato_id) REFERENCES contrato(id)
);

CREATE TABLE historial_suspension (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    fecha_suspension DATETIME NOT NULL,
    fecha_reactivacion DATETIME,
    dias_suspendidos INT DEFAULT 0,
    aplicado_en_recibo_id INT NULL, 
    FOREIGN KEY (contrato_id) REFERENCES contrato(id),
    FOREIGN KEY (aplicado_en_recibo_id) REFERENCES recibo(id)
);

CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recibo_id INT NOT NULL,
    fecha_pago DATETIME NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL, 
    nro_operacion VARCHAR(50),
    observaciones TEXT,
    empleado_registro_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recibo_id) REFERENCES recibo(id),
    FOREIGN KEY (empleado_registro_id) REFERENCES empleado(id)
);

CREATE TABLE notificacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- Antes ENUM
    estado VARCHAR(20) DEFAULT 'PENDIENTE', -- Antes ENUM
    fecha_envio DATETIME,
    mensaje TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);