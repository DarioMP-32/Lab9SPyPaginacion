
const API_BASE_URL = 'http://localhost:8080';

const CLAVE_TOKEN = 'jwt_token';


function decodificarJWT(token) {
    try {
        const payloadBase64Url = token.split('.')[1];
        const payloadBase64 = payloadBase64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(payloadBase64)
                .split('')
                .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('No se pudo decodificar el token JWT:', error);
        return null;
    }
}

function obtenerToken() {
    return sessionStorage.getItem(CLAVE_TOKEN);
}

function obtenerClaims() {
    const token = obtenerToken();
    if (!token) return null;
    return decodificarJWT(token);
}

function tokenExpirado() {
    const claims = obtenerClaims();
    if (!claims || !claims.exp) return true;
    return Date.now() >= claims.exp * 1000;
}

function obtenerUsername() {
    const claims = obtenerClaims();
    return (claims && claims.sub) || '';
}

function obtenerRoles() {
    const claims = obtenerClaims();
    return (claims && claims.roles) || [];
}

function tieneRol(rol) {
    return obtenerRoles().includes(rol);
}

function esAdmin() {
    return tieneRol('ROLE_ADMIN');
}

function esOperador() {
    return tieneRol('ROLE_OPERADOR');
}

function esConductor() {
    return tieneRol('ROLE_CONDUCTOR');
}

function guardarSesion(authResponseDTO) {
    sessionStorage.setItem(CLAVE_TOKEN, authResponseDTO.token);
}

function limpiarSesion() {
    sessionStorage.removeItem(CLAVE_TOKEN);
}

function cerrarSesion() {
    limpiarSesion();
    window.location.href = 'index.html';
}

function exigirSesion() {
    if (!obtenerToken() || tokenExpirado()) {
        limpiarSesion();
        window.location.href = 'index.html';
    }
}


async function fetchWithAuth(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${obtenerToken()}`,
        ...(options.headers || {}),
    };

    const respuesta = await fetch(url, { ...options, headers });

    if (respuesta.status === 401 || respuesta.status === 403) {
        limpiarSesion();
        window.location.href = 'index.html';
        throw new Error('Sesión expirada o sin permisos. Redirigiendo al login…');
    }

    if (!respuesta.ok) {
        const cuerpo = await respuesta.json().catch(() => null);
        let mensaje = (cuerpo && cuerpo.message) || `El servidor respondió con estado ${respuesta.status}`;

        if (cuerpo && Array.isArray(cuerpo.errores) && cuerpo.errores.length > 0) {
            const detalle = cuerpo.errores.map((e) => `${e.campo}: ${e.mensaje}`).join(' · ');
            mensaje = `${mensaje} (${detalle})`;
        }

        const error = new Error(mensaje);
        error.status = respuesta.status;
        error.cuerpo = cuerpo;
        throw error;
    }

    return respuesta;
}
