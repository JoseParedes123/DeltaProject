-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 26-10-2026 a las 05:26:13
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `logistica_delta`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `choferes`
--

CREATE TABLE `choferes` (
  `id_chofer` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `licencia` varchar(30) NOT NULL,
  `categoria_licencia` varchar(10) NOT NULL,
  `fecha_vencimiento_licencia` date NOT NULL,
  `estado` enum('ACTIVO','INACTIVO','SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `choferes`
--

INSERT INTO `choferes` (`id_chofer`, `id_usuario`, `licencia`, `categoria_licencia`, `fecha_vencimiento_licencia`, `estado`) VALUES
(1, 4, 'LIC-DELTA-001', 'B1', '2028-10-05', 'ACTIVO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id_cliente` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `tipo_cliente` enum('PARTICULAR','CORPORATIVO') NOT NULL DEFAULT 'PARTICULAR',
  `razon_social` varchar(150) DEFAULT NULL,
  `cuit_dni` varchar(20) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `estado_validacion` enum('PENDIENTE','VALIDADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
  `aprobado_corporativo` tinyint(1) NOT NULL DEFAULT 0,
  `descuento_porcentaje` decimal(5,2) NOT NULL DEFAULT 0.00,
  `fecha_registro` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id_cliente`, `id_usuario`, `tipo_cliente`, `razon_social`, `cuit_dni`, `telefono`, `direccion`, `estado_validacion`, `aprobado_corporativo`, `descuento_porcentaje`, `fecha_registro`) VALUES
(1, 1, 'CORPORATIVO', 'Delta Demo', '30-00000000-0', '1100000000', 'Av. Logística 123', 'VALIDADO', 1, 0.00, '2026-10-05 11:12:42'),
(2, 5, 'PARTICULAR', NULL, '30111226', '1100000003', 'Av. Demo 456', 'VALIDADO', 0, 0.00, '2026-10-05 13:05:34'),
(3, 21, 'PARTICULAR', NULL, NULL, NULL, NULL, 'PENDIENTE', 0, 0.00, '2026-10-19 16:27:51'),
(4, 22, 'PARTICULAR', NULL, NULL, NULL, NULL, 'PENDIENTE', 0, 0.00, '2026-10-21 17:45:02');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comprobantes`
--

CREATE TABLE `comprobantes` (
  `id_comprobante` int(11) NOT NULL,
  `id_envio` int(11) NOT NULL,
  `numero_comprobante` varchar(30) NOT NULL,
  `fecha_emision` datetime NOT NULL DEFAULT current_timestamp(),
  `tipo` varchar(40) NOT NULL,
  `ruta_archivo` varchar(255) DEFAULT NULL,
  `id_usuario_emisor` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `consultas_web`
--

CREATE TABLE `consultas_web` (
  `id_consulta` int(11) NOT NULL,
  `id_cliente` int(11) DEFAULT NULL,
  `id_envio` int(11) DEFAULT NULL,
  `nombre` varchar(120) NOT NULL,
  `email` varchar(120) NOT NULL,
  `asunto` varchar(80) NOT NULL,
  `mensaje` text NOT NULL,
  `estado` enum('PENDIENTE','EN_REVISION','RESPONDIDA') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `contactos`
--

CREATE TABLE `contactos` (
  `id_contacto` int(11) NOT NULL,
  `tipo` enum('REMITENTE','DESTINATARIO') NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `email` varchar(120) DEFAULT NULL,
  `direccion` varchar(200) NOT NULL,
  `id_zona` int(11) DEFAULT NULL,
  `id_cliente` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `contactos`
--

INSERT INTO `contactos` (`id_contacto`, `tipo`, `nombre`, `telefono`, `email`, `direccion`, `id_zona`, `id_cliente`) VALUES
(1, 'REMITENTE', 'Cliente Demo', '1100000001', 'remitente@delta.local', 'CABA', 1, 1),
(2, 'DESTINATARIO', 'María González', '1100000002', 'maria@delta.local', 'CABA', 1, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `depositos`
--

CREATE TABLE `depositos` (
  `id_deposito` int(11) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `direccion` varchar(200) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `depositos`
--

INSERT INTO `depositos` (`id_deposito`, `nombre`, `direccion`, `activo`) VALUES
(1, 'Central Delta', 'Av. Logística 123, Buenos Aires', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `envios`
--

CREATE TABLE `envios` (
  `id_envio` int(11) NOT NULL,
  `codigo_envio` varchar(20) NOT NULL,
  `id_cliente` int(11) NOT NULL,
  `id_remitente` int(11) NOT NULL,
  `id_destinatario` int(11) NOT NULL,
  `id_paquete` int(11) NOT NULL,
  `estado` enum('REGISTRADO','RECIBIDO_DEPOSITO','PREPARADO','EN_TRANSITO','EN_REPARTO','ENTREGADO','NO_ENTREGADO','DEVUELTO','EXTRAVIADO','CANCELADO') NOT NULL DEFAULT 'REGISTRADO',
  `id_zona_destino` int(11) NOT NULL,
  `id_tarifa_aplicada` int(11) DEFAULT NULL,
  `precio_final` decimal(12,2) DEFAULT NULL,
  `id_ruta` int(11) DEFAULT NULL,
  `orden_parada` int(11) DEFAULT NULL,
  `fecha_solicitud` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_entrega_estimada` date DEFAULT NULL,
  `id_usuario_registro` int(11) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `envios`
--

INSERT INTO `envios` (`id_envio`, `codigo_envio`, `id_cliente`, `id_remitente`, `id_destinatario`, `id_paquete`, `estado`, `id_zona_destino`, `id_tarifa_aplicada`, `precio_final`, `id_ruta`, `orden_parada`, `fecha_solicitud`, `fecha_entrega_estimada`, `id_usuario_registro`, `activo`) VALUES
(1, 'ENV-0001', 1, 1, 2, 1, 'RECIBIDO_DEPOSITO', 1, NULL, NULL, NULL, NULL, '2026-10-05 11:12:42', NULL, 1, 1),
(2, 'ENV-0002', 1, 1, 2, 2, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-05 11:12:42', NULL, 1, 1),
(3, 'ENV-0003', 1, 1, 2, 3, 'EN_TRANSITO', 1, NULL, NULL, 1, NULL, '2026-10-05 11:12:42', NULL, 1, 1),
(4, 'ENV-0004', 1, 1, 2, 4, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-05 11:12:42', NULL, 1, 1),
(5, 'ENV-0005', 2, 1, 2, 5, 'RECIBIDO_DEPOSITO', 1, NULL, NULL, NULL, NULL, '2026-10-05 13:05:34', NULL, 5, 1),
(6, 'ENV-0006', 2, 1, 2, 6, 'REGISTRADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(7, 'ENV-0007', 1, 1, 2, 7, 'RECIBIDO_DEPOSITO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(8, 'ENV-0008', 2, 1, 2, 8, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(9, 'ENV-0009', 1, 1, 2, 9, 'DEVUELTO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(10, 'ENV-0010', 2, 1, 2, 10, 'REGISTRADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(11, 'ENV-0011', 1, 1, 2, 11, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(12, 'ENV-0012', 2, 1, 2, 12, 'PREPARADO', 1, NULL, NULL, 2, 1, '2026-10-19 15:40:16', NULL, 1, 1),
(13, 'ENV-0013', 1, 1, 2, 13, 'DEVUELTO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(14, 'ENV-0014', 2, 1, 2, 14, 'REGISTRADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(15, 'ENV-0015', 1, 1, 2, 15, 'RECIBIDO_DEPOSITO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(16, 'ENV-0016', 2, 1, 2, 16, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(17, 'ENV-0017', 1, 1, 2, 17, 'DEVUELTO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(18, 'ENV-0018', 2, 1, 2, 18, 'REGISTRADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(19, 'ENV-0019', 1, 1, 2, 19, 'RECIBIDO_DEPOSITO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(20, 'ENV-0020', 2, 1, 2, 20, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(21, 'ENV-0021', 1, 1, 2, 21, 'DEVUELTO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(22, 'ENV-0022', 2, 1, 2, 22, 'REGISTRADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(23, 'ENV-0023', 1, 1, 2, 23, 'RECIBIDO_DEPOSITO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(24, 'ENV-0024', 2, 1, 2, 24, 'PREPARADO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1),
(25, 'ENV-0025', 1, 1, 2, 25, 'DEVUELTO', 1, NULL, NULL, NULL, NULL, '2026-10-19 15:40:16', NULL, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `historial`
--

CREATE TABLE `historial` (
  `id_historial` int(11) NOT NULL,
  `tipo_evento` enum('CAMBIO_ESTADO','MODIFICACION') NOT NULL,
  `tabla_afectada` varchar(60) NOT NULL,
  `registro_id` int(11) NOT NULL,
  `campo_modificado` varchar(60) DEFAULT NULL,
  `valor_anterior` varchar(255) DEFAULT NULL,
  `valor_nuevo` varchar(255) DEFAULT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `historial`
--

INSERT INTO `historial` (`id_historial`, `tipo_evento`, `tabla_afectada`, `registro_id`, `campo_modificado`, `valor_anterior`, `valor_nuevo`, `id_usuario`, `fecha_hora`, `descripcion`) VALUES
(1, 'CAMBIO_ESTADO', 'envios', 4, 'estado', NULL, 'DEVUELTO', 1, '2026-10-05 12:33:52', 'Cambio realizado desde Gestión de Depósito'),
(2, 'CAMBIO_ESTADO', 'envios', 4, 'estado', NULL, 'PREPARADO', 1, '2026-10-05 16:48:13', 'Cambio realizado desde Gestión de Depósito'),
(3, 'CAMBIO_ESTADO', 'envios', 5, 'estado', NULL, 'RECIBIDO_DEPOSITO', 2, '2026-10-05 20:13:44', 'Cambio realizado desde Gestión de Depósito'),
(4, 'MODIFICACION', 'envios', 12, 'id_ruta', NULL, 'Ruta #2', 13, '2026-10-26 01:16:16', 'Asignado al chofer Carlos Rodríguez (ruta #2, parada 1)'),
(5, 'CAMBIO_ESTADO', 'envios', 11, 'estado', NULL, 'PREPARADO', 6, '2026-10-26 01:17:04', 'Cambio realizado desde Gestión de Depósito');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `incidencias`
--

CREATE TABLE `incidencias` (
  `id_incidencia` int(11) NOT NULL,
  `id_envio` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `lugar` varchar(150) NOT NULL,
  `id_usuario_registro` int(11) NOT NULL,
  `tipo` enum('EXTRAVIO','DANIO','DIRECCION_INCORRECTA','PROBLEMA_ENTREGA','PROBLEMA_PAQUETE','OTRO') NOT NULL,
  `descripcion` text NOT NULL,
  `estado` enum('ABIERTA','EN_INVESTIGACION','RESUELTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
  `resolucion` text DEFAULT NULL,
  `fecha_resolucion` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `incidencias`
--

INSERT INTO `incidencias` (`id_incidencia`, `id_envio`, `fecha`, `hora`, `lugar`, `id_usuario_registro`, `tipo`, `descripcion`, `estado`, `resolucion`, `fecha_resolucion`) VALUES
(1, 3, '2026-10-05', '19:09:10', 'Gestión de Logística Delta', 4, 'DIRECCION_INCORRECTA', 'el plebe me dio una ubicacion incorrecta', 'ABIERTA', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `intentos_entrega`
--

CREATE TABLE `intentos_entrega` (
  `id_intento` int(11) NOT NULL,
  `id_envio` int(11) NOT NULL,
  `numero_intento` tinyint(4) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `id_chofer` int(11) NOT NULL,
  `resultado` enum('EXITOSO','FALLIDO') NOT NULL,
  `motivo_falla` enum('DESTINATARIO_AUSENTE','DIRECCION_INCORRECTA','DOMICILIO_INACCESIBLE','RECHAZO_DESTINATARIO','PROBLEMA_PAQUETE','OTRO') DEFAULT NULL,
  `receptor_nombre` varchar(120) DEFAULT NULL,
  `receptor_documento` varchar(20) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos_deposito`
--

CREATE TABLE `movimientos_deposito` (
  `id_movimiento` int(11) NOT NULL,
  `id_paquete` int(11) NOT NULL,
  `id_ubicacion_anterior` int(11) DEFAULT NULL,
  `id_ubicacion_nueva` int(11) NOT NULL,
  `es_actual` tinyint(1) NOT NULL DEFAULT 1,
  `id_usuario` int(11) NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  `motivo` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `movimientos_deposito`
--

INSERT INTO `movimientos_deposito` (`id_movimiento`, `id_paquete`, `id_ubicacion_anterior`, `id_ubicacion_nueva`, `es_actual`, `id_usuario`, `fecha_hora`, `motivo`) VALUES
(1, 1, NULL, 1, 1, 1, '2026-10-05 11:12:42', 'Recepción en depósito central'),
(2, 2, NULL, 2, 1, 1, '2026-10-05 11:12:42', 'Clasificación de paquete frágil'),
(3, 4, NULL, 3, 0, 1, '2026-10-05 11:12:42', 'Ingreso y clasificación'),
(4, 4, 3, 4, 1, 1, '2026-10-05 12:34:13', 'fue devuelto y se cambio la clasificacion'),
(5, 5, NULL, 3, 1, 2, '2026-10-14 19:19:37', 'llego el pedido papu'),
(6, 12, NULL, 5, 1, 13, '2026-10-19 15:41:24', 'se preparo bro'),
(7, 6, NULL, 45, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(8, 7, NULL, 44, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(9, 8, NULL, 43, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(10, 9, NULL, 42, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(11, 10, NULL, 41, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(12, 11, NULL, 40, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(13, 13, NULL, 38, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(14, 14, NULL, 37, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(15, 15, NULL, 36, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(16, 16, NULL, 35, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(17, 17, NULL, 34, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(18, 18, NULL, 33, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(19, 19, NULL, 32, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(20, 20, NULL, 31, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(21, 21, NULL, 30, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(22, 22, NULL, 29, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(23, 23, NULL, 28, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(24, 24, NULL, 27, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración'),
(25, 25, NULL, 26, 1, 1, '2026-10-19 15:45:17', 'Ubicación inicial de demostración');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos_seguimiento`
--

CREATE TABLE `movimientos_seguimiento` (
  `id_movimiento` int(11) NOT NULL,
  `id_envio` int(11) NOT NULL,
  `sucursal` varchar(100) NOT NULL,
  `provincia` varchar(80) NOT NULL,
  `detalle` varchar(255) NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `movimientos_seguimiento`
--

INSERT INTO `movimientos_seguimiento` (`id_movimiento`, `id_envio`, `sucursal`, `provincia`, `detalle`, `fecha_hora`) VALUES
(1, 1, 'Sucursal Logística Delta CABA', 'Ciudad Autónoma de Buenos Aires', 'El paquete fue recibido en sucursal.', '2026-10-05 11:12:42'),
(2, 2, 'Sucursal Logística Delta CABA', 'Ciudad Autónoma de Buenos Aires', 'El paquete ingresó y quedó listo para clasificación.', '2026-10-05 11:12:42'),
(3, 3, 'Centro de distribución CABA', 'Ciudad Autónoma de Buenos Aires', 'El paquete salió del centro de distribución.', '2026-10-05 11:12:42'),
(4, 4, 'Sucursal Logística Delta CABA', 'Ciudad Autónoma de Buenos Aires', 'El paquete ingresó a revisión por una incidencia.', '2026-10-05 11:12:42'),
(5, 5, 'Sucursal Logística Delta', 'Argentina', 'Solicitud recibida. El paquete espera su recepción en sucursal.', '2026-10-14 14:29:29'),
(6, 6, 'Sucursal Logística Delta', 'Argentina', 'Solicitud recibida. El paquete espera su recepción en sucursal.', '2026-10-14 16:24:03'),
(7, 7, 'Sucursal Logística Delta', 'Argentina', 'Solicitud recibida. El paquete espera su recepción en sucursal.', '2026-10-14 17:59:21');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `paquetes`
--

CREATE TABLE `paquetes` (
  `id_paquete` int(11) NOT NULL,
  `codigo` varchar(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `peso_kg` decimal(10,3) NOT NULL,
  `largo_cm` decimal(10,2) DEFAULT NULL,
  `ancho_cm` decimal(10,2) DEFAULT NULL,
  `alto_cm` decimal(10,2) DEFAULT NULL,
  `cantidad_bultos` int(11) NOT NULL DEFAULT 1,
  `tipo_mercaderia` varchar(80) NOT NULL,
  `valor_declarado` decimal(12,2) DEFAULT NULL,
  `es_fragil` tinyint(1) NOT NULL DEFAULT 0,
  `fecha_registro` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `paquetes`
--

INSERT INTO `paquetes` (`id_paquete`, `codigo`, `descripcion`, `peso_kg`, `largo_cm`, `ancho_cm`, `alto_cm`, `cantidad_bultos`, `tipo_mercaderia`, `valor_declarado`, `es_fragil`, `fecha_registro`) VALUES
(1, 'DEL-0001', 'Caja de repuestos', 8.500, NULL, NULL, NULL, 1, 'Repuestos', NULL, 0, '2026-10-05 11:12:42'),
(2, 'DEL-0002', 'Monitor profesional', 4.200, NULL, NULL, NULL, 1, 'Electrónica', NULL, 1, '2026-10-05 11:12:42'),
(3, 'DEL-0003', 'Documentación empresarial', 1.100, NULL, NULL, NULL, 1, 'Documentos', NULL, 0, '2026-10-05 11:12:42'),
(4, 'DEL-0004', 'Equipamiento informático', 12.700, NULL, NULL, NULL, 1, 'Electrónica', NULL, 1, '2026-10-05 11:12:42'),
(5, 'DEL-0005', 'Pedido de cliente demo', 2.500, NULL, NULL, NULL, 1, 'Accesorios', NULL, 0, '2026-10-05 13:05:34'),
(6, 'DEL-0006', 'Teclado y mouse inalámbricos', 1.250, NULL, NULL, NULL, 1, 'Electrónica', 85000.00, 0, '2026-10-19 15:40:16'),
(7, 'DEL-0007', 'Notebook para oficina', 2.100, NULL, NULL, NULL, 1, 'Informática', 950000.00, 1, '2026-10-19 15:40:16'),
(8, 'DEL-0008', 'Documentos contables', 0.450, NULL, NULL, NULL, 1, 'Documentos', 15000.00, 1, '2026-10-19 15:40:16'),
(9, 'DEL-0009', 'Repuestos de impresora', 3.800, NULL, NULL, NULL, 1, 'Repuestos', 72000.00, 0, '2026-10-19 15:40:16'),
(10, 'DEL-0010', 'Lámpara de escritorio', 1.700, NULL, NULL, NULL, 1, 'Hogar', 32000.00, 0, '2026-10-19 15:40:16'),
(11, 'DEL-0011', 'Cámara fotográfica', 0.900, NULL, NULL, NULL, 1, 'Electrónica', 410000.00, 1, '2026-10-19 15:40:16'),
(12, 'DEL-0012', 'Caja de libros', 6.200, NULL, NULL, NULL, 1, 'Libros', 68000.00, 0, '2026-10-19 15:40:16'),
(13, 'DEL-0013', 'Herramientas manuales', 5.400, NULL, NULL, NULL, 1, 'Herramientas', 120000.00, 0, '2026-10-19 15:40:16'),
(14, 'DEL-0014', 'Monitor de 24 pulgadas', 4.600, NULL, NULL, NULL, 1, 'Electrónica', 260000.00, 1, '2026-10-19 15:40:16'),
(15, 'DEL-0015', 'Ropa y accesorios', 2.300, NULL, NULL, NULL, 1, 'Indumentaria', 95000.00, 0, '2026-10-19 15:40:16'),
(16, 'DEL-0016', 'Piezas de cerámica', 3.100, NULL, NULL, NULL, 1, 'Frágil', 78000.00, 1, '2026-10-19 15:40:16'),
(17, 'DEL-0017', 'Cargadores y cables', 1.050, NULL, NULL, NULL, 1, 'Accesorios', 48000.00, 0, '2026-10-19 15:40:16'),
(18, 'DEL-0018', 'Silla ergonómica desarmada', 8.800, NULL, NULL, NULL, 1, 'Mobiliario', 185000.00, 0, '2026-10-19 15:40:16'),
(19, 'DEL-0019', 'Tablet para capacitación', 0.750, NULL, NULL, NULL, 1, 'Electrónica', 320000.00, 1, '2026-10-19 15:40:16'),
(20, 'DEL-0020', 'Material de laboratorio', 4.250, NULL, NULL, NULL, 1, 'Insumos', 210000.00, 1, '2026-10-19 15:40:16'),
(21, 'DEL-0021', 'Juguetes didácticos', 2.850, NULL, NULL, NULL, 1, 'Juguetes', 54000.00, 0, '2026-10-19 15:40:16'),
(22, 'DEL-0022', 'Caja de herramientas eléctricas', 7.600, NULL, NULL, NULL, 1, 'Herramientas', 240000.00, 0, '2026-10-19 15:40:16'),
(23, 'DEL-0023', 'Repuestos de bicicleta', 3.500, NULL, NULL, NULL, 1, 'Repuestos', 63000.00, 0, '2026-10-19 15:40:16'),
(24, 'DEL-0024', 'Equipo de audio pequeño', 5.200, NULL, NULL, NULL, 1, 'Electrónica', 175000.00, 1, '2026-10-19 15:40:16'),
(25, 'DEL-0025', 'Artículos de librería', 2.050, NULL, NULL, NULL, 1, 'Librería', 39000.00, 0, '2026-10-19 15:40:16');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rutas`
--

CREATE TABLE `rutas` (
  `id_ruta` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `origen` varchar(200) NOT NULL,
  `destino` varchar(200) NOT NULL,
  `recorrido` text DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `id_vehiculo` int(11) NOT NULL,
  `id_chofer` int(11) NOT NULL,
  `id_zona` int(11) DEFAULT NULL,
  `estado` enum('PLANIFICADA','EN_CURSO','FINALIZADA','CANCELADA') NOT NULL DEFAULT 'PLANIFICADA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `rutas`
--

INSERT INTO `rutas` (`id_ruta`, `fecha`, `origen`, `destino`, `recorrido`, `observaciones`, `id_vehiculo`, `id_chofer`, `id_zona`, `estado`) VALUES
(1, '2026-10-05', 'Depósito Central', 'CABA', 'Ruta demostrativa', 'Ruta creada para la presentación', 1, 1, 1, 'EN_CURSO'),
(2, '2026-10-26', 'Depósito Central', 'CABA', NULL, 'Ruta creada desde Gestión de Depósito', 1, 1, 1, 'PLANIFICADA');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tarifas`
--

CREATE TABLE `tarifas` (
  `id_tarifa` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `id_zona` int(11) NOT NULL,
  `tipo_envio` varchar(60) NOT NULL,
  `precio_base` decimal(10,2) NOT NULL,
  `precio_por_kg` decimal(10,2) NOT NULL DEFAULT 0.00,
  `precio_por_m3` decimal(10,2) NOT NULL DEFAULT 0.00,
  `recargo_fragil` decimal(10,2) NOT NULL DEFAULT 0.00,
  `vigente_desde` date NOT NULL,
  `vigente_hasta` date DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `tarifas`
--

INSERT INTO `tarifas` (`id_tarifa`, `nombre`, `id_zona`, `tipo_envio`, `precio_base`, `precio_por_kg`, `precio_por_m3`, `recargo_fragil`, `vigente_desde`, `vigente_hasta`, `activo`) VALUES
(1, 'Envío estándar CABA', 1, 'ESTANDAR', 2500.00, 350.00, 0.00, 500.00, '2026-01-01', NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ubicaciones`
--

CREATE TABLE `ubicaciones` (
  `id_ubicacion` int(11) NOT NULL,
  `id_deposito` int(11) NOT NULL,
  `sector` varchar(20) NOT NULL,
  `estante` varchar(20) NOT NULL,
  `posicion` varchar(20) NOT NULL,
  `ocupada` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `ubicaciones`
--

INSERT INTO `ubicaciones` (`id_ubicacion`, `id_deposito`, `sector`, `estante`, `posicion`, `ocupada`) VALUES
(1, 1, 'A', '01', '01', 1),
(2, 1, 'A', '01', '02', 1),
(3, 1, 'B', '02', '01', 1),
(4, 1, 'B', '02', '02', 1),
(5, 1, 'C', '03', '01', 1),
(6, 1, 'G', '02', '05', 0),
(7, 1, 'G', '02', '04', 0),
(8, 1, 'G', '02', '03', 0),
(9, 1, 'G', '02', '02', 0),
(10, 1, 'G', '02', '01', 0),
(11, 1, 'G', '01', '05', 0),
(12, 1, 'G', '01', '04', 0),
(13, 1, 'G', '01', '03', 0),
(14, 1, 'G', '01', '02', 0),
(15, 1, 'G', '01', '01', 0),
(16, 1, 'F', '02', '05', 0),
(17, 1, 'F', '02', '04', 0),
(18, 1, 'F', '02', '03', 0),
(19, 1, 'F', '02', '02', 0),
(20, 1, 'F', '02', '01', 0),
(21, 1, 'F', '01', '05', 0),
(22, 1, 'F', '01', '04', 0),
(23, 1, 'F', '01', '03', 0),
(24, 1, 'F', '01', '02', 0),
(25, 1, 'F', '01', '01', 0),
(26, 1, 'E', '02', '05', 1),
(27, 1, 'E', '02', '04', 1),
(28, 1, 'E', '02', '03', 1),
(29, 1, 'E', '02', '02', 1),
(30, 1, 'E', '02', '01', 1),
(31, 1, 'E', '01', '05', 1),
(32, 1, 'E', '01', '04', 1),
(33, 1, 'E', '01', '03', 1),
(34, 1, 'E', '01', '02', 1),
(35, 1, 'E', '01', '01', 1),
(36, 1, 'D', '02', '05', 1),
(37, 1, 'D', '02', '04', 1),
(38, 1, 'D', '02', '03', 1),
(39, 1, 'D', '02', '02', 0),
(40, 1, 'D', '02', '01', 1),
(41, 1, 'D', '01', '05', 1),
(42, 1, 'D', '01', '04', 1),
(43, 1, 'D', '01', '03', 1),
(44, 1, 'D', '01', '02', 1),
(45, 1, 'D', '01', '01', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `apellido` varchar(80) NOT NULL,
  `dni` varchar(15) DEFAULT NULL,
  `email` varchar(120) NOT NULL,
  `nombre_usuario` varchar(60) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` enum('ADMINISTRADOR','OPERADOR_DEPOSITO','CHOFER','ADMINISTRACION','CLIENTE') NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `ultimo_login` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre`, `apellido`, `dni`, `email`, `nombre_usuario`, `password_hash`, `rol`, `activo`, `fecha_creacion`, `ultimo_login`) VALUES
(1, 'Juan', 'Pérez', '30111222', 'juan.perez@delta.local', 'operador', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'OPERADOR_DEPOSITO', 1, '2026-10-05 11:12:42', NULL),
(2, 'Ana', 'García', '30111223', 'ana.admin@delta.local', 'admin', '73ec58e5352101ebeaf6b279c20d2aeae80da07789b26bce731ed79fc4a943c2', 'ADMINISTRADOR', 1, '2026-10-05 13:05:34', NULL),
(3, 'Laura', 'Sosa', '30111224', 'laura.admin@delta.local', 'administracion', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRACION', 1, '2026-10-05 13:05:34', NULL),
(4, 'Carlos', 'Rodríguez', '30111225', 'carlos.chofer@delta.local', 'chofer', 'b3299b6890452207676ec7cf6c9cda6ffa0f682a227c72db166f33c4211f7885', 'CHOFER', 1, '2026-10-05 13:05:34', NULL),
(5, 'Sofía', 'Martínez', '30111226', 'sofia.cliente@delta.local', 'cliente', '09a31a7001e261ab1e056182a71d3cf57f582ca9a29cff5eb83be0f0549730a9', 'CLIENTE', 1, '2026-10-05 13:05:34', NULL),
(6, 'Mariana', 'López', '30111227', 'mariana.lopez@delta.local', 'mlopez', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1, '2026-10-19 15:34:04', NULL),
(7, 'Federico', 'Ramírez', '30111228', 'federico.ramirez@delta.local', 'framirez', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1, '2026-10-19 15:34:04', NULL),
(8, 'Valentina', 'Fernández', '30111229', 'valentina.fernandez@delta.local', 'vfernandez', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1, '2026-10-19 15:34:04', NULL),
(9, 'Diego', 'Torres', '30111230', 'diego.torres@delta.local', 'dtorres', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1, '2026-10-19 15:34:04', NULL),
(10, 'Camila', 'Ruiz', '30111231', 'camila.ruiz@delta.local', 'cruiz', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1, '2026-10-19 15:34:04', NULL),
(11, 'Lucas', 'Gómez', '30111232', 'lucas.gomez@delta.local', 'lgomez', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(12, 'Florencia', 'Díaz', '30111233', 'florencia.diaz@delta.local', 'fdiaz', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(13, 'Mateo', 'Acosta', '30111234', 'mateo.acosta@delta.local', 'macosta', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(14, 'Julieta', 'Benítez', '30111235', 'julieta.benitez@delta.local', 'jbenitez', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(15, 'Nicolás', 'Molina', '30111236', 'nicolas.molina@delta.local', 'nmolina', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(16, 'Agustina', 'Herrera', '30111237', 'agustina.herrera@delta.local', 'aherrera', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(17, 'Tomás', 'Castro', '30111238', 'tomas.castro@delta.local', 'tcastro', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'OPERADOR_DEPOSITO', 1, '2026-10-19 15:34:04', NULL),
(18, 'Paula', 'Medina', '30111239', 'paula.medina@delta.local', 'pmedina', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'CHOFER', 1, '2026-10-19 15:34:04', NULL),
(19, 'Bruno', 'Suárez', '30111240', 'bruno.suarez@delta.local', 'bsuarez', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'CHOFER', 1, '2026-10-19 15:34:04', NULL),
(20, 'Micaela', 'Ortega', '30111241', 'micaela.ortega@delta.local', 'mortega', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'CHOFER', 1, '2026-10-19 15:34:04', NULL),
(21, 'Spider-', '-Black', NULL, 'josearielparedesotiniano@gmail.com', 'josearielparedesotiniano@gmail.com', '$2y$10$Wyb21xp7kFf7k/ROVbWPnel0/I0UBhZ4kmL0s5Nx.4e2UjnyI/Z3m', 'CLIENTE', 1, '2026-10-19 16:27:51', NULL),
(22, 'pepito', 'pepitovich', NULL, 'pepito@gmail.com', 'pepe', '$2y$10$749QfmJxd4SJwZeiPIiLAObAkgg5ktU8cEXi4itIKt4ovlCdR2ToO', 'CLIENTE', 1, '2026-10-21 17:45:02', NULL),
(23, '0', '0', NULL, '0', 'SIXSVENE', '15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225', 'CLIENTE', 1, '2026-10-26 00:34:20', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `vehiculos`
--

CREATE TABLE `vehiculos` (
  `id_vehiculo` int(11) NOT NULL,
  `patente` varchar(15) NOT NULL,
  `tipo` varchar(40) NOT NULL,
  `marca` varchar(50) DEFAULT NULL,
  `modelo` varchar(50) DEFAULT NULL,
  `anio` smallint(6) DEFAULT NULL,
  `capacidad_peso_kg` decimal(10,2) NOT NULL,
  `capacidad_volumen_m3` decimal(10,2) NOT NULL,
  `estado` enum('DISPONIBLE','EN_RUTA','MANTENIMIENTO','INACTIVO') NOT NULL DEFAULT 'DISPONIBLE',
  `id_zona_restringida` int(11) DEFAULT NULL,
  `motivo_restriccion` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `vehiculos`
--

INSERT INTO `vehiculos` (`id_vehiculo`, `patente`, `tipo`, `marca`, `modelo`, `anio`, `capacidad_peso_kg`, `capacidad_volumen_m3`, `estado`, `id_zona_restringida`, `motivo_restriccion`) VALUES
(1, 'AB123CD', 'Furgón', 'Mercedes-Benz', 'Sprinter', 2024, 1200.00, 10.00, 'DISPONIBLE', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `zonas`
--

CREATE TABLE `zonas` (
  `id_zona` int(11) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `cobertura` enum('CABA_GBA','INTERIOR') NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `zonas`
--

INSERT INTO `zonas` (`id_zona`, `nombre`, `cobertura`, `descripcion`, `activo`) VALUES
(1, 'CABA', 'CABA_GBA', 'Cobertura CABA', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `choferes`
--
ALTER TABLE `choferes`
  ADD PRIMARY KEY (`id_chofer`),
  ADD UNIQUE KEY `uq_choferes_usuario` (`id_usuario`),
  ADD UNIQUE KEY `uq_choferes_licencia` (`licencia`);

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id_cliente`),
  ADD UNIQUE KEY `uq_clientes_usuario` (`id_usuario`);

--
-- Indices de la tabla `comprobantes`
--
ALTER TABLE `comprobantes`
  ADD PRIMARY KEY (`id_comprobante`),
  ADD UNIQUE KEY `uq_comprobantes_numero` (`numero_comprobante`),
  ADD KEY `fk_comprobantes_envio` (`id_envio`),
  ADD KEY `fk_comprobantes_usuario` (`id_usuario_emisor`);

--
-- Indices de la tabla `consultas_web`
--
ALTER TABLE `consultas_web`
  ADD PRIMARY KEY (`id_consulta`),
  ADD KEY `idx_consultas_estado_fecha` (`estado`,`fecha_creacion`),
  ADD KEY `fk_consultas_cliente` (`id_cliente`),
  ADD KEY `fk_consultas_envio` (`id_envio`);

--
-- Indices de la tabla `contactos`
--
ALTER TABLE `contactos`
  ADD PRIMARY KEY (`id_contacto`),
  ADD KEY `fk_contactos_zona` (`id_zona`),
  ADD KEY `fk_contactos_cliente` (`id_cliente`);

--
-- Indices de la tabla `depositos`
--
ALTER TABLE `depositos`
  ADD PRIMARY KEY (`id_deposito`);

--
-- Indices de la tabla `envios`
--
ALTER TABLE `envios`
  ADD PRIMARY KEY (`id_envio`),
  ADD UNIQUE KEY `uq_envios_codigo` (`codigo_envio`),
  ADD UNIQUE KEY `uq_envios_paquete` (`id_paquete`),
  ADD KEY `fk_envios_remitente` (`id_remitente`),
  ADD KEY `fk_envios_destinatario` (`id_destinatario`),
  ADD KEY `fk_envios_zona` (`id_zona_destino`),
  ADD KEY `fk_envios_tarifa` (`id_tarifa_aplicada`),
  ADD KEY `fk_envios_ruta` (`id_ruta`),
  ADD KEY `fk_envios_usuario_reg` (`id_usuario_registro`),
  ADD KEY `idx_envios_estado` (`estado`),
  ADD KEY `idx_envios_cliente` (`id_cliente`);

--
-- Indices de la tabla `historial`
--
ALTER TABLE `historial`
  ADD PRIMARY KEY (`id_historial`),
  ADD KEY `fk_historial_usuario` (`id_usuario`),
  ADD KEY `idx_historial_registro` (`tabla_afectada`,`registro_id`);

--
-- Indices de la tabla `incidencias`
--
ALTER TABLE `incidencias`
  ADD PRIMARY KEY (`id_incidencia`),
  ADD KEY `fk_incidencias_usuario` (`id_usuario_registro`),
  ADD KEY `idx_incidencias_envio` (`id_envio`);

--
-- Indices de la tabla `intentos_entrega`
--
ALTER TABLE `intentos_entrega`
  ADD PRIMARY KEY (`id_intento`),
  ADD UNIQUE KEY `uq_intento_envio_numero` (`id_envio`,`numero_intento`),
  ADD KEY `fk_intentos_chofer` (`id_chofer`),
  ADD KEY `idx_intentos_envio` (`id_envio`);

--
-- Indices de la tabla `movimientos_deposito`
--
ALTER TABLE `movimientos_deposito`
  ADD PRIMARY KEY (`id_movimiento`),
  ADD KEY `fk_movdep_ubic_ant` (`id_ubicacion_anterior`),
  ADD KEY `fk_movdep_ubic_nueva` (`id_ubicacion_nueva`),
  ADD KEY `fk_movdep_usuario` (`id_usuario`),
  ADD KEY `idx_movdep_paquete` (`id_paquete`);

--
-- Indices de la tabla `movimientos_seguimiento`
--
ALTER TABLE `movimientos_seguimiento`
  ADD PRIMARY KEY (`id_movimiento`),
  ADD KEY `idx_seguimiento_envio_fecha` (`id_envio`,`fecha_hora`);

--
-- Indices de la tabla `paquetes`
--
ALTER TABLE `paquetes`
  ADD PRIMARY KEY (`id_paquete`),
  ADD UNIQUE KEY `uq_paquetes_codigo` (`codigo`),
  ADD KEY `idx_paquetes_codigo` (`codigo`);

--
-- Indices de la tabla `rutas`
--
ALTER TABLE `rutas`
  ADD PRIMARY KEY (`id_ruta`),
  ADD KEY `fk_rutas_vehiculo` (`id_vehiculo`),
  ADD KEY `fk_rutas_chofer` (`id_chofer`),
  ADD KEY `fk_rutas_zona` (`id_zona`);

--
-- Indices de la tabla `tarifas`
--
ALTER TABLE `tarifas`
  ADD PRIMARY KEY (`id_tarifa`),
  ADD KEY `fk_tarifas_zona` (`id_zona`);

--
-- Indices de la tabla `ubicaciones`
--
ALTER TABLE `ubicaciones`
  ADD PRIMARY KEY (`id_ubicacion`),
  ADD UNIQUE KEY `uq_ubicacion` (`id_deposito`,`sector`,`estante`,`posicion`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `uq_usuarios_email` (`email`),
  ADD UNIQUE KEY `uq_usuarios_nombre_usuario` (`nombre_usuario`),
  ADD UNIQUE KEY `uq_usuarios_dni` (`dni`);

--
-- Indices de la tabla `vehiculos`
--
ALTER TABLE `vehiculos`
  ADD PRIMARY KEY (`id_vehiculo`),
  ADD UNIQUE KEY `uq_vehiculos_patente` (`patente`),
  ADD KEY `fk_vehiculos_zona` (`id_zona_restringida`);

--
-- Indices de la tabla `zonas`
--
ALTER TABLE `zonas`
  ADD PRIMARY KEY (`id_zona`),
  ADD UNIQUE KEY `uq_zonas_nombre` (`nombre`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `choferes`
--
ALTER TABLE `choferes`
  MODIFY `id_chofer` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id_cliente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `comprobantes`
--
ALTER TABLE `comprobantes`
  MODIFY `id_comprobante` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `contactos`
--
ALTER TABLE `contactos`
  MODIFY `id_contacto` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `depositos`
--
ALTER TABLE `depositos`
  MODIFY `id_deposito` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `envios`
--
ALTER TABLE `envios`
  MODIFY `id_envio` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT de la tabla `historial`
--
ALTER TABLE `historial`
  MODIFY `id_historial` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `incidencias`
--
ALTER TABLE `incidencias`
  MODIFY `id_incidencia` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `intentos_entrega`
--
ALTER TABLE `intentos_entrega`
  MODIFY `id_intento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `movimientos_deposito`
--
ALTER TABLE `movimientos_deposito`
  MODIFY `id_movimiento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT de la tabla `paquetes`
--
ALTER TABLE `paquetes`
  MODIFY `id_paquete` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT de la tabla `rutas`
--
ALTER TABLE `rutas`
  MODIFY `id_ruta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `tarifas`
--
ALTER TABLE `tarifas`
  MODIFY `id_tarifa` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `ubicaciones`
--
ALTER TABLE `ubicaciones`
  MODIFY `id_ubicacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT de la tabla `vehiculos`
--
ALTER TABLE `vehiculos`
  MODIFY `id_vehiculo` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `zonas`
--
ALTER TABLE `zonas`
  MODIFY `id_zona` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `choferes`
--
ALTER TABLE `choferes`
  ADD CONSTRAINT `fk_choferes_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD CONSTRAINT `fk_clientes_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `comprobantes`
--
ALTER TABLE `comprobantes`
  ADD CONSTRAINT `fk_comprobantes_envio` FOREIGN KEY (`id_envio`) REFERENCES `envios` (`id_envio`),
  ADD CONSTRAINT `fk_comprobantes_usuario` FOREIGN KEY (`id_usuario_emisor`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `contactos`
--
ALTER TABLE `contactos`
  ADD CONSTRAINT `fk_contactos_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  ADD CONSTRAINT `fk_contactos_zona` FOREIGN KEY (`id_zona`) REFERENCES `zonas` (`id_zona`);

--
-- Filtros para la tabla `envios`
--
ALTER TABLE `envios`
  ADD CONSTRAINT `fk_envios_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  ADD CONSTRAINT `fk_envios_destinatario` FOREIGN KEY (`id_destinatario`) REFERENCES `contactos` (`id_contacto`),
  ADD CONSTRAINT `fk_envios_paquete` FOREIGN KEY (`id_paquete`) REFERENCES `paquetes` (`id_paquete`),
  ADD CONSTRAINT `fk_envios_remitente` FOREIGN KEY (`id_remitente`) REFERENCES `contactos` (`id_contacto`),
  ADD CONSTRAINT `fk_envios_ruta` FOREIGN KEY (`id_ruta`) REFERENCES `rutas` (`id_ruta`),
  ADD CONSTRAINT `fk_envios_tarifa` FOREIGN KEY (`id_tarifa_aplicada`) REFERENCES `tarifas` (`id_tarifa`),
  ADD CONSTRAINT `fk_envios_usuario_reg` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuarios` (`id_usuario`),
  ADD CONSTRAINT `fk_envios_zona` FOREIGN KEY (`id_zona_destino`) REFERENCES `zonas` (`id_zona`);

--
-- Filtros para la tabla `historial`
--
ALTER TABLE `historial`
  ADD CONSTRAINT `fk_historial_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `incidencias`
--
ALTER TABLE `incidencias`
  ADD CONSTRAINT `fk_incidencias_envio` FOREIGN KEY (`id_envio`) REFERENCES `envios` (`id_envio`),
  ADD CONSTRAINT `fk_incidencias_usuario` FOREIGN KEY (`id_usuario_registro`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `intentos_entrega`
--
ALTER TABLE `intentos_entrega`
  ADD CONSTRAINT `fk_intentos_chofer` FOREIGN KEY (`id_chofer`) REFERENCES `choferes` (`id_chofer`),
  ADD CONSTRAINT `fk_intentos_envio` FOREIGN KEY (`id_envio`) REFERENCES `envios` (`id_envio`);

--
-- Filtros para la tabla `movimientos_deposito`
--
ALTER TABLE `movimientos_deposito`
  ADD CONSTRAINT `fk_movdep_paquete` FOREIGN KEY (`id_paquete`) REFERENCES `paquetes` (`id_paquete`),
  ADD CONSTRAINT `fk_movdep_ubic_ant` FOREIGN KEY (`id_ubicacion_anterior`) REFERENCES `ubicaciones` (`id_ubicacion`),
  ADD CONSTRAINT `fk_movdep_ubic_nueva` FOREIGN KEY (`id_ubicacion_nueva`) REFERENCES `ubicaciones` (`id_ubicacion`),
  ADD CONSTRAINT `fk_movdep_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `rutas`
--
ALTER TABLE `rutas`
  ADD CONSTRAINT `fk_rutas_chofer` FOREIGN KEY (`id_chofer`) REFERENCES `choferes` (`id_chofer`),
  ADD CONSTRAINT `fk_rutas_vehiculo` FOREIGN KEY (`id_vehiculo`) REFERENCES `vehiculos` (`id_vehiculo`),
  ADD CONSTRAINT `fk_rutas_zona` FOREIGN KEY (`id_zona`) REFERENCES `zonas` (`id_zona`);

--
-- Filtros para la tabla `tarifas`
--
ALTER TABLE `tarifas`
  ADD CONSTRAINT `fk_tarifas_zona` FOREIGN KEY (`id_zona`) REFERENCES `zonas` (`id_zona`);

--
-- Filtros para la tabla `ubicaciones`
--
ALTER TABLE `ubicaciones`
  ADD CONSTRAINT `fk_ubicaciones_deposito` FOREIGN KEY (`id_deposito`) REFERENCES `depositos` (`id_deposito`);

--
-- Filtros para la tabla `vehiculos`
--
ALTER TABLE `vehiculos`
  ADD CONSTRAINT `fk_vehiculos_zona` FOREIGN KEY (`id_zona_restringida`) REFERENCES `zonas` (`id_zona`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
