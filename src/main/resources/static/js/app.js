"use strict";
document.querySelector(".mobile-toggle")?.addEventListener("click", function () {
  const open = document.querySelector(".sidebar").classList.toggle("menu-open");
  this.setAttribute("aria-expanded", String(open));
});
document.querySelectorAll("nav a").forEach(a => {
  if (a.pathname === "/" ? location.pathname === "/" : location.pathname.startsWith(a.pathname)) {
    a.classList.add("active"); a.setAttribute("aria-current", "page");
  }
});
document.querySelectorAll("form[data-confirm]").forEach(form => form.addEventListener("submit", e => {
  if (!confirm(form.dataset.confirm)) e.preventDefault();
}));
document.querySelectorAll(".print-button").forEach(b => b.addEventListener("click", () => window.print()));
document.querySelectorAll(".back-button").forEach(b => b.addEventListener("click", () => history.back()));
// La misma convención COP que el servidor: punto de miles y coma de centavos.
function parseMoney(raw) {
  const value = raw.trim();
  if (/^(?:[0-9]+|[1-9][0-9]{0,2}(?:\.[0-9]{3})+)(?:,[0-9]{1,2})?$/.test(value))
    return Number(value.replaceAll(".", "").replace(",", "."));
  if (/^[0-9]+\.[0-9]{1,2}$/.test(value)) return Number(value);
  return NaN;
}
document.addEventListener("input", event => {
  if (event.target.matches(".money-input, [data-money]")) {
    const input = event.target;
    input.setCustomValidity(!input.value.trim() || Number.isFinite(parseMoney(input.value)) ? "" : "Escribe 1300, 1.300 o 1.300,50.");
  }
});
document.addEventListener("focusout", event => {
  if (!event.target.matches(".money-input, [data-money]") || !event.target.value.trim()) return;
  const amount = parseMoney(event.target.value);
  if (Number.isFinite(amount)) event.target.value = new Intl.NumberFormat("es-CO", { maximumFractionDigits: 2 }).format(amount);
});
const operation = document.querySelector(".operation-form");
document.querySelector("#retirar-form")?.addEventListener("submit", event => {
  if (!document.querySelector('input[form="retirar-form"][name="ids"]:checked')) {
    event.preventDefault(); alert("Marca al menos un registro para revisar su retiro.");
  }
});
const initialUnits = document.querySelector("#unidades_iniciales");
const productForm = document.querySelector("[data-product-form]");
if (productForm) {
  const type = productForm.querySelector("#tipo");
  const updateProductType = () => {
    const service = type.value === "SERVICIO";
    productForm.querySelectorAll("[data-physical-field]").forEach(field => {
      field.hidden = service;
      if (field.tagName === "FIELDSET") field.disabled = service;
      field.querySelectorAll("input, select").forEach(input => { input.disabled = service; });
    });
    productForm.querySelector("[data-service-help]").hidden = !service;
  };
  type.addEventListener("change", updateProductType);
  updateProductType();
}
function stockLabel(quantity, unit) {
  const count = Number(quantity);
  const kind = (unit || "").trim().toUpperCase();
  const label = kind === "PAQUETE" ? (count === 1 ? "paquete" : "paquetes")
    : kind === "UNIDAD" ? (count === 1 ? "unidad" : "unidades") : kind.toLowerCase();
  return count + " " + label;
}
function isActive(value) { return value === true || value === 1 || value === "1"; }
function watchStock(url, status, update, button) {
  let refreshing = false;
  const refresh = async () => {
    if (refreshing || document.hidden) return;
    refreshing = true;
    if (button) button.disabled = true;
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);
    try {
      const response = await fetch(url, { cache: "no-store", signal: controller.signal, headers: { "Accept": "application/json" } });
      if (!response.ok || response.redirected) throw new Error("No disponible");
      const records = await response.json();
      if (!Array.isArray(records)) throw new Error("Respuesta inválida");
      update(new Map(records.map(p => [String(p.id), p])));
      status.textContent = "Existencias actualizadas a las " + new Date().toLocaleTimeString("es-CO") + ". Actualización automática cada 15 segundos.";
      status.classList.remove("text-danger");
    } catch (error) {
      status.textContent = "No se pudo consultar el stock actual. Los valores mostrados pueden estar desactualizados. Revisa la conexión o inicia sesión de nuevo.";
      status.classList.add("text-danger");
    } finally {
      clearTimeout(timeout); refreshing = false;
      if (button) button.disabled = false;
    }
  };
  button?.addEventListener("click", refresh);
  window.addEventListener("focus", refresh);
  window.addEventListener("pageshow", refresh);
  document.addEventListener("visibilitychange", refresh);
  setInterval(refresh, 15000);
  refresh();
}
if (initialUnits) {
  const presentation = document.querySelector("#unidad_medida");
  const updateInitialCost = () => {
    const packageSale = presentation.value === "PAQUETE";
    document.querySelector("#costo_inicial").required = Number(initialUnits.value) > 0;
    document.querySelector('label[for="unidades_iniciales"]').textContent = packageSale ? "Paquetes completos al comenzar" : "Unidades al comenzar";
    document.querySelector('label[for="costo_inicial"]').textContent = packageSale ? "Costo de cada paquete (COP)" : "Costo de cada unidad (COP)";
    document.querySelector("#initial-stock-preview").textContent = initialUnits.value !== "" && presentation.value
      ? "Al guardar tendrás " + stockLabel(initialUnits.value, presentation.value) + " disponibles." : "";
  };
  initialUnits.addEventListener("input", updateInitialCost);
  presentation.addEventListener("change", updateInitialCost); updateInitialCost();
}
const adjustment = document.querySelector("#ajustes");
if (adjustment) {
  const updatePreview = () => {
    const selected = adjustment.querySelector("#productoId").selectedOptions[0];
    const quantity = Number(adjustment.querySelector("#cantidad").value || 0);
    const incoming = adjustment.querySelector("#tipo").value === "AJUSTE_ENTRADA";
    adjustment.querySelector("#costo").required = incoming;
    const stock = Number(selected?.dataset.stock);
    const quantityInput = adjustment.querySelector("#cantidad");
    quantityInput.setCustomValidity(!incoming && quantity > stock ? "La salida supera las existencias disponibles." : "");
    adjustment.querySelector("#costo").disabled = !incoming;
    adjustment.querySelector("#stock-preview").textContent = Number.isFinite(stock)
      ? "Disponibles: " + stockLabel(stock, selected.dataset.unidad) + " → después del registro: " + stockLabel(stock + (incoming ? quantity : -quantity), selected.dataset.unidad) : "";
  };
  adjustment.addEventListener("input", updatePreview); adjustment.addEventListener("change", updatePreview); updatePreview();
}
document.querySelectorAll("[data-live-stock]").forEach(section => {
  watchStock(section.dataset.stockUrl, section.querySelector(".stock-sync-status"), records => {
    section.querySelectorAll("[data-stock-id]").forEach(row => {
      const record = records.get(row.dataset.stockId);
      row.querySelector("[data-stock-value]").textContent = record
        ? (record.tipo === "SERVICIO" ? "Servicio · sin inventario" : stockLabel(record.stock_actual, record.unidad_medida)) : "No disponible";
      const stockAction = row.querySelector("[data-stock-action]");
      if (stockAction) stockAction.hidden = !record || record.tipo === "SERVICIO";
      if (!record) return;
      const minimum = row.querySelector("[data-stock-min]");
      if (minimum) minimum.textContent = Number(record.stock_minimo);
      const state = row.querySelector("[data-stock-status]");
      if (state) state.textContent = !isActive(record.activo) ? "Inactivo" : Number(record.stock_actual) <= Number(record.stock_minimo) ? "Reponer" : "Disponible";
    });
    if (adjustment) {
      const select = adjustment.querySelector("#productoId");
      for (const option of select.options) {
        if (!option.value) continue;
        const record = records.get(option.value);
        option.disabled = !record || !isActive(record.activo) || record.tipo === "SERVICIO";
        if (record) {
          option.dataset.stock = record.stock_actual; option.dataset.unidad = record.unidad_medida;
          option.textContent = record.nombre + " · disponibles: " + stockLabel(record.stock_actual, record.unidad_medida);
        }
      }
      adjustment.dispatchEvent(new Event("change"));
    }
  });
});
if (operation) {
  const lines = operation.querySelector(".operation-lines");
  const prototype = lines.firstElementChild.cloneNode(true);
  const money = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", minimumFractionDigits: 0, maximumFractionDigits: 2 });
  const renumber = () => lines.querySelectorAll(".operation-line").forEach((line, i) => {
    line.querySelectorAll("[name]").forEach(input => { input.name = input.name.replace(/lineas\[\d+\]/, "lineas[" + i + "]"); });
  });
  const calculate = () => {
    let subtotal = 0;
    lines.querySelectorAll(".operation-line").forEach(line => {
      const product = line.querySelector(".product-choice");
      const service = line.querySelector(".service-choice");
      const chosen = product.value ? product : service;
      const price = operation.dataset.tipo === "compras" ? parseMoney(line.querySelector(".line-cost").value || "0") : Number(chosen?.selectedOptions[0]?.dataset.precio || 0);
      const total = Math.round(price * Number(line.querySelector(".line-quantity").value || 0) * 100) / 100;
      subtotal += total; line.querySelector(".line-total").textContent = money.format(total) + " COP";
      const availability = line.querySelector(".stock-available");
      if (availability) {
        const isService = product.value && product.selectedOptions[0]?.dataset.tipo === "SERVICIO";
        const available = Number(product.selectedOptions[0]?.dataset.stock);
        const quantity = Number(line.querySelector(".line-quantity").value || 0);
        availability.textContent = isService ? "Servicio · no requiere existencias." : product.value && Number.isFinite(available)
          ? "Disponibles: " + stockLabel(available, product.selectedOptions[0].dataset.unidad) + " · al confirmar quedarían: " + stockLabel(available - quantity, product.selectedOptions[0].dataset.unidad) : "";
        availability.classList.toggle("text-danger", !isService && Boolean(product.value) && available < quantity);
      }
    });
    const total = subtotal - parseMoney(operation.querySelector("#descuento").value || "0");
    document.querySelector("#estimated-total").textContent = money.format(total) + " COP";
    const received = operation.querySelector("#valorRecibido");
    if (received) received.required = operation.querySelector("#metodoPago").value === "EFECTIVO";
  };
  const refreshButton = operation.querySelector(".refresh-stock");
  if (refreshButton) {
    const status = operation.querySelector(".stock-refresh-status");
    watchStock(refreshButton.dataset.url, status, stockById => {
      for (const select of [...lines.querySelectorAll(".product-choice"), prototype.querySelector(".product-choice")]) {
        for (const option of select.options) {
          if (!option.value) continue;
          const record = stockById.get(option.value);
          option.disabled = !record || !isActive(record.activo) || !["MERCANCIA", "SERVICIO"].includes(record.tipo);
          if (record) {
            option.dataset.stock = record.stock_actual;
            option.dataset.unidad = record.unidad_medida;
            option.dataset.tipo = record.tipo;
            option.textContent = record.codigo + " · " + record.nombre + (record.tipo === "SERVICIO"
              ? " (servicio · sin inventario)" : " (stock: " + stockLabel(record.stock_actual, record.unidad_medida) + ")");
          }
        }
      }
      calculate();
    }, refreshButton);
  }
  operation.querySelector(".add-line").addEventListener("click", () => {
    if (lines.children.length >= 100) return;
    lines.append(prototype.cloneNode(true)); renumber(); calculate();
  });
  lines.addEventListener("click", e => {
    if (e.target.closest(".remove-line") && lines.children.length > 1) {
      e.target.closest(".operation-line").remove(); renumber(); calculate();
    }
  });
  lines.addEventListener("change", e => {
    const line = e.target.closest(".operation-line");
    if (e.target.matches(".product-choice") && e.target.value && line.querySelector(".service-choice")) line.querySelector(".service-choice").value = "";
    if (e.target.matches(".service-choice") && e.target.value) line.querySelector(".product-choice").value = "";
    calculate();
  });
  operation.addEventListener("input", calculate);
  operation.addEventListener("change", calculate);
  operation.addEventListener("submit", e => {
    const keys = new Set();
    for (const line of lines.children) {
      const p = line.querySelector(".product-choice").value;
      const s = line.querySelector(".service-choice")?.value;
      const key = p ? "p" + p : "s" + s;
      if ((!p && !s) || keys.has(key)) {
        e.preventDefault(); alert("Selecciona un artículo distinto por línea. Reúne las cantidades repetidas."); return;
      }
      keys.add(key);
    }
  });
  calculate();
}
