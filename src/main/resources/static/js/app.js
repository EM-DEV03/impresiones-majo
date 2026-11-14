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
  if (Number.isFinite(amount)) event.target.value = new Intl.NumberFormat("es-CO", {maximumFractionDigits:2}).format(amount);
});
const operation = document.querySelector(".operation-form");
document.querySelector("#retirar-form")?.addEventListener("submit", event => {
  if (!document.querySelector('input[form="retirar-form"][name="ids"]:checked')) {
    event.preventDefault(); alert("Marca al menos un registro para revisar su retiro.");
  }
});
const initialUnits = document.querySelector("#unidades_iniciales");
if (initialUnits) {
  const updateInitialCost = () => { document.querySelector("#costo_inicial").required = Number(initialUnits.value) > 0; };
  initialUnits.addEventListener("input", updateInitialCost); updateInitialCost();
}
const adjustment = document.querySelector("#ajustes");
if (adjustment) {
  const updatePreview = () => {
    const selected = adjustment.querySelector("#productoId").selectedOptions[0];
    const quantity = Number(adjustment.querySelector("#cantidad").value || 0);
    const incoming = adjustment.querySelector("#tipo").value === "AJUSTE_ENTRADA";
    adjustment.querySelector("#costo").required = incoming;
    const stock = Number(selected?.dataset.stock);
    adjustment.querySelector("#stock-preview").textContent = Number.isFinite(stock)
      ? "Disponibles: " + stock + " → después del registro: " + (stock + (incoming ? quantity : -quantity)) : "";
  };
  adjustment.addEventListener("input", updatePreview); adjustment.addEventListener("change", updatePreview); updatePreview();
}
if (operation) {
  const lines = operation.querySelector(".operation-lines");
  const prototype = lines.firstElementChild.cloneNode(true);
  const money = new Intl.NumberFormat("es-CO", {style:"currency", currency:"COP", minimumFractionDigits:0, maximumFractionDigits:2});
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
        const available = Number(product.selectedOptions[0]?.dataset.stock);
        const quantity = Number(line.querySelector(".line-quantity").value || 0);
        availability.textContent = product.value && Number.isFinite(available)
          ? "Disponibles: " + available + " · al confirmar quedarían: " + (available - quantity) : "";
        availability.classList.toggle("text-danger", Boolean(product.value) && available < quantity);
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
    let refreshing = false;
    const refresh = async () => {
      if (refreshing) return;
      refreshing = true; refreshButton.disabled = true;
      try {
        const response = await fetch(refreshButton.dataset.url, {cache:"no-store", headers:{"Accept":"application/json"}});
        if (!response.ok || response.redirected) throw new Error("No disponible");
        const records = await response.json();
        const stockById = new Map(records.map(p => [String(p.id), p]));
        for (const select of [...lines.querySelectorAll(".product-choice"), prototype.querySelector(".product-choice")]) {
          for (const option of select.options) {
            if (!option.value) continue;
            const record = stockById.get(option.value);
            option.disabled = !record || !record.activo;
            if (record) {
              option.dataset.stock = record.stock_actual;
              option.textContent = record.codigo + " · " + record.nombre + " (stock: " + Number(record.stock_actual) + ")";
            }
          }
        }
        calculate();
        status.textContent = "Existencias actualizadas. La cantidad disponible se comprueba de nuevo al confirmar.";
      } catch (error) {
        status.textContent = "No se pudieron actualizar las existencias. Revisa tu conexión o vuelve a iniciar sesión.";
      } finally { refreshing = false; refreshButton.disabled = false; }
    };
    refreshButton.addEventListener("click", refresh);
    window.addEventListener("focus", refresh);
    refresh();
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
