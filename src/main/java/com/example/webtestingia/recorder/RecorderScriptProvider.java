package com.example.webtestingia.recorder;

import org.springframework.stereotype.Component;

/**
 * Provides the JavaScript snippet injected into the browser to capture DOM events and
 * forward them to the recorder backend automatically.
 */
@Component
public class RecorderScriptProvider {

    private static final String RECORDER_SCRIPT = """
// Script de Web Recorder para inyectar en el navegador
(function () {
  // Evitar inyectar dos veces
  if (window.__webRecorderInjected) {
    return;
  }
  window.__webRecorderInjected = true;

  const defaultConfig = {
    browserId: '',
    sessionId: '',
    baseUrl: 'http://localhost:9090', // puedes sobreescribirlo desde el backend
  };

  const config = Object.assign({}, defaultConfig, window.__recorderConfig || {});

  if (!config.browserId || !config.sessionId) {
    console.warn('[WebRecorder] Faltan browserId o sessionId en window.__recorderConfig');
  }

  function getCssSelector(el) {
    if (!el || !(el instanceof Element)) return '';

    // Si tiene id, es lo más directo
    if (el.id) {
      return '#' + el.id;
    }

    // Si tiene name, se puede usar
    if (el.getAttribute('name')) {
      return el.tagName.toLowerCase() + '[name="' + el.getAttribute('name') + '"]';
    }

    // Construir un selector simple con clase o tag
    let selector = el.tagName.toLowerCase();

    if (el.classList && el.classList.length > 0) {
      selector += '.' + Array.from(el.classList).join('.');
    }

    return selector;
  }

  async function sendRecorderEvent(action, target, extra) {
    if (!config.browserId || !config.sessionId) {
      console.warn('[WebRecorder] No se enviará evento: faltan browserId/sessionId');
      return;
    }

    const el = target instanceof Element ? target : null;

    const selector = el ? 'css=' + getCssSelector(el) : (extra && extra.selector) || '';
    const text =
      extra && typeof extra.text === 'string'
        ? extra.text
        : el && (el.innerText || el.textContent || '');
    const value =
      extra && typeof extra.value !== 'undefined'
        ? extra.value
        : el && typeof el.value !== 'undefined'
        ? el.value
        : '';

    const payload = {
      browserId: config.browserId,
      sessionId: config.sessionId,
      action: action,
      selector: selector,
      text: text || '',
      value: value || '',
    };

    try {
      const url = config.baseUrl.replace(/\/+$/, '') + '/api/recorder/event';

      await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
        // keepalive ayuda en algunas navegaciones, pero no es obligatorio
        keepalive: true,
      });
    } catch (e) {
      console.error('[WebRecorder] Error enviando evento', e);
    }
  }

  // CLICK: mapea a action = "click"
  function handleClick(e) {
    const target = e.target;
    sendRecorderEvent('click', target, {});
  }

  // CHANGE: mapea a "change" para selects, checkbox, radio
  function handleChange(e) {
    const target = e.target;
    if (!target) return;

    const tag = target.tagName.toLowerCase();
    const type = (target.type || '').toLowerCase();

    // select / checkbox / radio -> change
    if (tag === 'select' || type === 'checkbox' || type === 'radio') {
      sendRecorderEvent('change', target, { value: target.value });
      return;
    }

    // otros inputs podrían mapearse como input
    sendRecorderEvent('input', target, { value: target.value });
  }

  // INPUT directo (para campos de texto)
  function handleInput(e) {
    const target = e.target;
    if (!target) return;

    const tag = target.tagName.toLowerCase();
    const type = (target.type || '').toLowerCase();

    // Solo nos interesan inputs de texto, textarea, etc.
    if (
      tag === 'input' &&
      (type === 'text' ||
        type === 'email' ||
        type === 'password' ||
        type === 'search' ||
        type === 'tel' ||
        type === 'url' ||
        type === 'number')
    ) {
      sendRecorderEvent('input', target, { value: target.value });
    } else if (tag === 'textarea') {
      sendRecorderEvent('input', target, { value: target.value });
    }
  }

  // ENTER en inputs -> input con value actual
  function handleKeydown(e) {
    if (e.key !== 'Enter') return;
    const target = e.target;
    if (!target) return;

    const tag = target.tagName.toLowerCase();

    if (tag === 'input' || tag === 'textarea') {
      sendRecorderEvent('input', target, { value: target.value });
    }
  }

  // SUBMIT: captura envíos de formularios
  function handleSubmit(e) {
    const target = e.target;
    sendRecorderEvent('submit', target, {});
  }

  // Navegación: podemos mandar un evento "navigate" cuando carga la página
  function sendInitialNavigate() {
    try {
      const href = window.location.href;
      sendRecorderEvent('navigate', null, { selector: '', text: '', value: href });
    } catch (e) {
      console.error('[WebRecorder] Error enviando evento navigate inicial', e);
    }
  }

  // Opcional: capturar cambios de URL mediante popstate / hashchange
  function handleUrlChange() {
    const href = window.location.href;
    sendRecorderEvent('navigate', null, { selector: '', text: '', value: href });
  }

  // Suscribir listeners en fase de captura para abarcar bubbling y delegación
  document.addEventListener('click', handleClick, true);
  document.addEventListener('change', handleChange, true);
  document.addEventListener('input', handleInput, true);
  document.addEventListener('keydown', handleKeydown, true);
  document.addEventListener('submit', handleSubmit, true);

  window.addEventListener('hashchange', handleUrlChange, true);
  window.addEventListener('popstate', handleUrlChange, true);

  // Navegación inicial
  sendInitialNavigate();

  console.log('[WebRecorder] Script de recorder inyectado correctamente');
})();
""";

    public String buildScript(String baseUrl, String sessionId, String browserId) {
        String sanitizedBase = sanitize(baseUrl == null || baseUrl.isBlank() ? "http://localhost:9090" : baseUrl);
        String safeBase = escape(sanitizedBase);
        String configScript = "window.__recorderConfig = Object.assign({}, window.__recorderConfig || {}, {" +
                "browserId:'" + escape(browserId) + "'," +
                "sessionId:'" + escape(sessionId) + "'," +
                "baseUrl:'" + safeBase + "'" +
                "});";
        return configScript + "\n" + RECORDER_SCRIPT;
    }

    private String sanitize(String value) {
        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
