/**
 * cspizza — Punto de venta (POS)
 * Maneja el carrito de productos para registrar un pedido completo:
 * catálogo por categorías, buscador, botones - / +, observación por línea,
 * total en vivo y generación de campos items[i].* para el formulario Thymeleaf.
 */

document.addEventListener('DOMContentLoaded', function () {
    const productosData = window.__productos__ || [];
    const categoriasData = window.__categorias__ || [];

    const tbody = document.getElementById('cuerpoDetalle');
    const totalEl = document.getElementById('totalPedido');
    const contadorEl = document.getElementById('contadorItems');
    const totalInput = document.getElementById('totalPedidoInput');
    const categoriaSelect = document.getElementById('filtroCategoria');
    const buscadorInput = document.getElementById('buscadorProducto');
    const catalogoGrid = document.getElementById('catalogoProductos');

    let items = []; // { idProducto, nombre, precio, cantidad, observacion }
    let rowIndex = 0;

    // --- Render del catálogo ---
    function renderCatalogo() {
        const filtroCat = categoriaSelect.value;
        const texto = buscadorInput.value.toLowerCase().trim();

        const filtrados = productosData.filter(p => {
            const coincideCat = !filtroCat || p.idCategoria == filtroCat;
            const coincideTexto = !texto ||
                p.nombre.toLowerCase().includes(texto);
            return coincideCat && coincideTexto && p.stock > 0 && p.estado === 'ACTIVO';
        });

        catalogoGrid.innerHTML = '';

        if (filtrados.length === 0) {
            catalogoGrid.innerHTML =
                '<div class="empty-state" style="text-align:center; padding:40px 20px;">' +
                '<div class="empty-icon">&#127850;</div>' +
                '<h3 style="margin:0 8px 4px; font-size:1.1rem;">No hay productos disponibles</div>' +
                '<p style="margin:0; color:#666; font-size:0.9rem;">Ajusta los filtros o revisa el stock.</p>' +
                '</div>';
            return;
        }

        filtrados.forEach(p => {
            const enCarrito = items.find(i => i.idProducto === p.idProducto);
            const enCarritoTxt = enCarrito ? ' (' + enCarrito.cantidad + ')' : '';
            const btnTexto = enCarrito ? 'Agregar más' : 'Agregar';

            const card = document.createElement('article');
            card.className = 'card producto-card';
            card.style = 'display:flex; flex-direction:column; gap:10px; padding:16px; min-width:220px;';
            card.innerHTML =
                '<div style="flex:1;">' +
                '<div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">' +
                '<h4 style="margin:0; font-size:1.05rem; font-weight:800;">' + escapeHtml(p.nombre) + '</h4>' +
                '<span class="badge badge-activo" style="font-size:0.75rem;">Stock: ' + p.stock + '</span>' +
                '</div>' +
                '<p style="margin:6px 0; font-size:0.85rem; color:#666;">' + escapeHtml(p.descripcion || '') + '</p>' +
                '<div style="display:flex; justify-content:space-between; align-items:center; margin-top:auto;">' +
                '<strong>$' + parseFloat(p.precioUnitario).toFixed(2) + '</strong>' +
                '<button type="button" class="btn btn-primary btn-sm" onclick="agregarProducto(' + p.idProducto + ')">' +
                btnTexto + enCarritoTxt + '</button>' +
                '</div>' +
                '</div>';
            catalogoGrid.appendChild(card);
        });
    }

    window.agregarProducto = function (idProducto) {
        const p = productosData.find(x => x.idProducto === idProducto);
        if (!p) return;

        const existente = items.find(i => i.idProducto === idProducto);
        if (existente) {
            if (existente.cantidad >= p.stock) {
                alert('No hay más stock disponible de "' + p.nombre + '".');
                return;
            }
            existente.cantidad += 1;
        } else {
            items.push({
                idProducto: p.idProducto,
                nombre: p.nombre,
                precio: parseFloat(p.precioUnitario),
                cantidad: 1,
                observacion: ''
            });
        }
        renderDetalle();
        renderCatalogo();
    };

window.agregarManual = function () {
        const input = document.getElementById('manualProducto');
        const cantInput = document.getElementById('manualCantidad');
        const nombre = (input.value || '').trim().toLowerCase();
        const cant = parseInt(cantInput.value) || 1;
        if (!nombre) { alert('Escribe el nombre del producto.'); return; }
        const p = productosData.find(x => x.nombre.toLowerCase() === nombre)
                || productosData.find(x => x.nombre.toLowerCase().includes(nombre));
        if (!p) { alert('Producto no encontrado: ' + input.value); return; }
        if (cant < 1) { alert('La cantidad debe ser mayor que cero.'); return; }
        if (p.stock < cant) { alert('Stock insuficiente de "' + p.nombre + '". Disponible: ' + p.stock); return; }
        const existente = items.find(i => i.idProducto === p.idProducto);
        if (existente) {
            if (existente.cantidad + cant > p.stock) {
                alert('No hay suficiente stock de "' + p.nombre + '".');
                return;
            }
            existente.cantidad += cant;
        } else {
            items.push({
                idProducto: p.idProducto,
                nombre: p.nombre,
                precio: parseFloat(p.precioUnitario),
                cantidad: cant,
                observacion: ''
            });
        }
        input.value = '';
        if (cantInput) cantInput.value = 1;
        renderDetalle();
        renderCatalogo();
    };

    window.quitarProducto = function (index) {
        items.splice(index, 1);
        renderDetalle();
        renderCatalogo();
    };

    window.cambiarCantidad = function (index, delta) {
        const item = items[index];
        const p = productosData.find(x => x.idProducto === item.idProducto);
        const nuevo = item.cantidad + delta;
        if (nuevo < 1) {
            quitarProducto(index);
            return;
        }
        if (nuevo > p.stock) {
            alert('No hay suficiente stock de "' + p.nombre + '".');
            return;
        }
        item.cantidad = nuevo;
        renderDetalle();
        renderCatalogo();
    };

    window.cambiarObservacion = function (index, valor) {
        items[index].observacion = valor;
    };

    // --- Render del detalle (carrito) ---
    function renderDetalle() {
        tbody.innerHTML = '';
        let total = 0;
        let cantidadTotal = 0;

        if (items.length === 0) {
            tbody.innerHTML =
                '<tr><td colspan="5" style="padding:24px; text-align:center; color:#666;">' +
                'No hay productos en el pedido aún.' +
                '</td></tr>';
        }

        items.forEach((item, index) => {
            const subtotal = item.precio * item.cantidad;
            total += subtotal;
            cantidadTotal += item.cantidad;

            const tr = document.createElement('tr');
            tr.innerHTML =
                '<td>' +
                escapeHtml(item.nombre) +
                '<input type="hidden" name="items[' + index + '].idProducto" value="' + item.idProducto + '">' +
                '</td>' +
                '<td>$' + item.precio.toFixed(2) + '</td>' +
                '<td>' +
                '<button type="button" class="btn btn-outline btn-sm" onclick="cambiarCantidad(' + index + ', -1)">&#8722;</button> ' +
                '<input type="number" name="items[' + index + '].cantidad" value="' + item.cantidad + '" min="1" max="99" style="width:50px;" onchange="validarCantidad(' + index + ', this)">' +
                ' <button type="button" class="btn btn-outline btn-sm" onclick="cambiarCantidad(' + index + ', 1)">&#43;</button>' +
                '</td>' +
                '<td>$' + subtotal.toFixed(2) + '</td>' +
                '<td>' +
                '<input type="text" name="items[' + index + '].observacion" value="' + escapeHtml(item.observacion || '') + '" placeholder="Observación (ej. sin cebolla)" style="width:140px;" onchange="cambiarObservacion(' + index + ', this.value)">' +
                ' <button type="button" class="btn btn-danger btn-sm" onclick="quitarProducto(' + index + ')">Quitar</button>' +
                '</td>';

            // Campos ocultos para binding Thymeleaf
            tr.querySelector('input[name="items[' + index + '].idProducto"]').name = 'items[' + index + '].idProducto';
            tr.querySelector('input[name="items[' + index + '].cantidad"]').name = 'items[' + index + '].cantidad';
            tr.querySelector('input[name="items[' + index + '].observacion"]').name = 'items[' + index + '].observacion';

            tbody.appendChild(tr);
        });

        totalEl.textContent = 'Total: $' + total.toFixed(2);
        contadorEl.textContent = '(' + cantidadTotal + ' ítem' + (cantidadTotal !== 1 ? 's' : '') + ')';
        totalInput.value = total.toFixed(2);
    }

    window.validarCantidad = function (index, input) {
        let val = parseInt(input.value) || 0;
        if (val < 1) val = 1;
        const p = productosData.find(x => x.idProducto === items[index].idProducto);
        if (val > p.stock) {
            val = p.stock;
            alert('Stock máximo disponible: ' + p.stock);
        }
        input.value = val;
        items[index].cantidad = val;
        renderDetalle();
        renderCatalogo();
    };

    function escapeHtml(texto) {
        if (!texto) return '';
        return texto
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // --- Inicialización ---
    categoriaSelect.addEventListener('change', renderCatalogo);
    buscadorInput.addEventListener('input', renderCatalogo);

    renderCatalogo();
    renderDetalle();
});
