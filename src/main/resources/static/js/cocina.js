/**
 * cspizza — Tablero de cocina
 * Renderiza pedidos por estado y se refresca cada 30 segundos.
 */

document.addEventListener('DOMContentLoaded', function () {
    const columnas = {
        pendiente: document.getElementById('columnaPendiente'),
        enPreparacion: document.getElementById('columnaEnPreparacion'),
        listo: document.getElementById('columnaListo'),
    };

    const ultimaActualizacion = document.getElementById('ultimaActualizacion');

    function renderizar(columnaId, pedidos) {
        const contenedor = columnas[columnaId];
        if (!contenedor) return;

        contenedor.innerHTML = '';

        if (pedidos.length === 0) {
            contenedor.innerHTML =
                '<div style="padding:24px; text-align:center; color:#666; font-size:0.9rem;">Sin pedidos</div>';
            return;
        }

        pedidos.forEach(p => {
            const tarjeta = document.createElement('article');
            tarjeta.style =
                'background:#fff; border:1px solid #e5e5e5; border-radius:10px; padding:14px; box-shadow:0 4px 14px rgba(0,0,0,0.06);';

            const tipoIcono = p.tipo === 'DOMICILIO' ? '&#127969;' : (p.tipo === 'RECOGER' ? '&#128736;' : '&#127867;');

            tarjeta.innerHTML =
                '<div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:8px;">' +
                '<strong style="font-size:0.95rem;">Pedido #' + p.id + '</strong>' +
                '<span style="font-size:0.78rem; color:#666;">' + p.fecha + '</span>' +
                '</div>' +
                '<div style="margin-bottom:8px; font-size:0.88rem;">' +
                '<span style="font-weight:700;">Cliente:</span> ' + escapeHtml(p.cliente) + '<br>' +
                '<span style="font-weight:700;">Tipo:</span> ' + tipoIcono + ' ' + escapeHtml(p.tipo) + '<br>' +
                '<span style="font-weight:700;">Dirección:</span> ' + escapeHtml(p.direccion || 'N/D') +
                '</div>' +
                '<div style="margin-bottom:8px; font-size:0.88rem;">' +
                '<span style="font-weight:700;">Productos:</span> ' + escapeHtml(p.producto) +
                '</div>' +
                '<div style="margin-top:12px; border-top:1px solid #e5e5e5; padding-top:8px;"></div>';

            contenedor.appendChild(tarjeta);
        });
    }

    function escapeHtml(texto) {
        if (!texto) return '';
        return String(texto)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function actualizar() {
        const ahora = new Date();
        ultimaActualizacion.textContent = 'Actualizado: ' +
            ahora.toLocaleDateString('es-CO') + ' ' + ahora.toLocaleTimeString('es-CO');

        renderizar('pendiente', window.__pendientes__ || []);
        renderizar('enPreparacion', window.__enPreparacion__ || []);
        renderizar('listo', window.__listo__ || []);
    }

    // Ejecutar inmediatamente y luego cada 30 segundos
    actualizar();
    setInterval(actualizar, 30000);
});
