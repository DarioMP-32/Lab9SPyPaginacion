const API_V1_ENVIOS = `${API_BASE_URL}/api/v1/envios`;

const PILL_CLASE = {
    ENTREGADO: 'pill-status--entregado',
    EN_TRANSITO: 'pill-status--en-transito',
    PENDIENTE: 'pill-status--pendiente',
    CANCELADO: 'pill-status--cancelado',
};
const PILL_TEXTO = {
    ENTREGADO: 'Entregado',
    EN_TRANSITO: 'En tránsito',
    PENDIENTE: 'Pendiente',
    CANCELADO: 'Cancelado',
};

const vista = {
    page: 0,
    size: 5,
    sortBy: 'fechaCreacion',
    direction: 'desc',
    busqueda: '',
    procedimiento: '',
    totalPages: 0,
};

const tbody = document.getElementById('tbodyEnvios');
const infoPaginador = document.getElementById('infoPaginador');
const modoActual = document.getElementById('modoActual');
const btnPrimera = document.getElementById('btnPrimera');
const btnAnterior = document.getElementById('btnAnterior');
const btnSiguiente = document.getElementById('btnSiguiente');
const btnUltima = document.getElementById('btnUltima');

function inicializarSesion() {
    document.getElementById('nombreUsuarioActual').textContent = obtenerUsername();
    const roles = obtenerRoles().map((r) => r.replace('ROLE_', ''));
    document.getElementById('rolUsuarioActual').textContent = roles.join(', ') || 'Sin rol';
    document.getElementById('btnLogout').addEventListener('click', cerrarSesion);
}

async function cargarDatos() {
    tbody.innerHTML = '<tr><td colspan="5">Cargando…</td></tr>';
    try {
        if (vista.procedimiento) {
            await cargarViaProcedimiento();
        } else {
            await cargarPaginado();
        }
    } catch (error) {
        mostrarMensajeTabla(`No se pudo cargar: ${error.message}`);
        infoPaginador.textContent = 'Error al cargar los datos';
        deshabilitarPaginador();
        console.error('Error al cargar envíos:', error);
    }
}

async function cargarPaginado() {
    const params = new URLSearchParams({
        page: vista.page,
        size: vista.size,
        sortBy: vista.sortBy,
        direction: vista.direction,
    });
    if (vista.busqueda) params.set('busqueda', vista.busqueda);

    const respuesta = await fetchWithAuth(`${API_V1_ENVIOS}?${params.toString()}`);
    const data = await respuesta.json();

    vista.totalPages = data.totalPages;
    modoActual.textContent = 'Consulta paginada desde la base de datos.';
    renderizarFilas(data.content);
    actualizarPaginador(data);
    actualizarIndicadoresOrden();
}

async function cargarViaProcedimiento() {
    const url = `${API_V1_ENVIOS}/procedimiento/${encodeURIComponent(vista.procedimiento)}`;
    const respuesta = await fetchWithAuth(url);
    const lista = await respuesta.json();

    modoActual.textContent = `Resultado de SP_OBTENER_ENVIOS_POR_ESTADO('${vista.procedimiento}')`;
    renderizarFilas(lista);
    infoPaginador.textContent = `Stored Procedure: ${lista.length} envío(s) con estado ${vista.procedimiento}`;
    deshabilitarPaginador();
}

function mostrarMensajeTabla(texto) {
    tbody.innerHTML = '';
    const fila = document.createElement('tr');
    const celda = document.createElement('td');
    celda.colSpan = 5;
    celda.textContent = texto;
    fila.appendChild(celda);
    tbody.appendChild(fila);
}

function crearCelda(texto) {
    const td = document.createElement('td');
    td.textContent = texto ?? '—';
    return td;
}

function renderizarFilas(envios) {
    if (!envios || envios.length === 0) {
        mostrarMensajeTabla('No hay envíos que coincidan con los filtros.');
        return;
    }

    tbody.innerHTML = '';
    envios.forEach((e) => {
        const fila = document.createElement('tr');

        fila.appendChild(crearCelda(e.codigoRastreo));
        fila.appendChild(crearCelda(e.destinatario));
        fila.appendChild(crearCelda(e.direccionDestino));
        fila.appendChild(crearCelda(
            '₡' + Number(e.montoFlete).toLocaleString('es-CR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })));

        const tdEstado = document.createElement('td');
        const pill = document.createElement('span');
        pill.className = `pill-status ${PILL_CLASE[e.estado] || 'pill-status--pendiente'}`;
        pill.textContent = PILL_TEXTO[e.estado] || e.estado;
        tdEstado.appendChild(pill);
        fila.appendChild(tdEstado);

        tbody.appendChild(fila);
    });
}

function actualizarPaginador(data) {
    const paginaMostrada = data.totalPages === 0 ? 0 : data.number + 1;
    infoPaginador.textContent =
        `Página ${paginaMostrada} de ${data.totalPages} (Total: ${data.totalElements} envíos)`;

    btnPrimera.disabled = data.first;
    btnAnterior.disabled = data.first;
    btnSiguiente.disabled = data.last;
    btnUltima.disabled = data.last;
}

function deshabilitarPaginador() {
    [btnPrimera, btnAnterior, btnSiguiente, btnUltima].forEach((b) => (b.disabled = true));
}

btnPrimera.addEventListener('click', () => { vista.page = 0; cargarDatos(); });
btnAnterior.addEventListener('click', () => { vista.page = Math.max(vista.page - 1, 0); cargarDatos(); });
btnSiguiente.addEventListener('click', () => { vista.page += 1; cargarDatos(); });
btnUltima.addEventListener('click', () => { vista.page = Math.max(vista.totalPages - 1, 0); cargarDatos(); });

function actualizarIndicadoresOrden() {
    document.querySelectorAll('.th-orden').forEach((btn) => {
        const th = btn.closest('th');
        const flecha = btn.querySelector('.th-flecha');
        if (btn.dataset.sort === vista.sortBy) {
            th.setAttribute('aria-sort', vista.direction === 'asc' ? 'ascending' : 'descending');
            flecha.textContent = vista.direction === 'asc' ? '▲' : '▼';
        } else {
            th.setAttribute('aria-sort', 'none');
            flecha.textContent = '';
        }
    });
}

document.querySelectorAll('.th-orden').forEach((btn) => {
    btn.addEventListener('click', () => {
        if (vista.procedimiento) return;
        if (vista.sortBy === btn.dataset.sort) {
            vista.direction = vista.direction === 'asc' ? 'desc' : 'asc';
        } else {
            vista.sortBy = btn.dataset.sort;
            vista.direction = 'asc';
        }
        vista.page = 0;
        cargarDatos();
    });
});

document.getElementById('formFiltros').addEventListener('submit', (evento) => {
    evento.preventDefault();
    vista.busqueda = document.getElementById('txtBusqueda').value.trim();
    vista.procedimiento = document.getElementById('selProcedimiento').value;
    vista.size = parseInt(document.getElementById('selTamano').value, 10);
    vista.page = 0;
    cargarDatos();
});

document.getElementById('selTamano').addEventListener('change', (evento) => {
    vista.size = parseInt(evento.target.value, 10);
    vista.page = 0;
    cargarDatos();
});

document.getElementById('selProcedimiento').addEventListener('change', (evento) => {
    vista.procedimiento = evento.target.value;
    vista.page = 0;
    cargarDatos();
});

document.getElementById('btnLimpiar').addEventListener('click', () => {
    document.getElementById('formFiltros').reset();
    Object.assign(vista, {
        page: 0, size: 5, sortBy: 'fechaCreacion', direction: 'desc', busqueda: '', procedimiento: '',
    });
    cargarDatos();
});

document.getElementById('btnMetricas').addEventListener('click', async () => {
    const tabla = document.getElementById('tablaMetricas');
    const cuerpo = document.getElementById('tbodyMetricas');
    try {
        const respuesta = await fetchWithAuth(`${API_V1_ENVIOS}/metricas`);
        const filas = await respuesta.json();

        cuerpo.innerHTML = '';
        filas.forEach((m) => {
            const tr = document.createElement('tr');
            tr.appendChild(crearCelda(PILL_TEXTO[m.estado] || m.estado));
            tr.appendChild(crearCelda(m.totalEnvios));
            tr.appendChild(crearCelda(
                '₡' + Number(m.totalFlete).toLocaleString('es-CR', { minimumFractionDigits: 2 })));
            cuerpo.appendChild(tr);
        });
        tabla.hidden = false;
    } catch (error) {
        alert(`No se pudo obtener las métricas: ${error.message}`);
        console.error('Error al cargar métricas:', error);
    }
});

inicializarSesion();
cargarDatos();
